package ke.ac.ku.ledgerly.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.ac.ku.ledgerly.auth.data.AuthRepository
import ke.ac.ku.ledgerly.data.model.FirestoreUserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val CURRENCY = stringPreferencesKey("currency")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val MONTHLY_BUDGET = stringPreferencesKey("monthly_budget")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
    }

    companion object {
        private const val TAG = "UserPreferencesRepo"
    }

    val userName: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.USER_NAME] ?: "" }
    val currency: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.CURRENCY] ?: "KES" }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }
    val monthlyBudget: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.MONTHLY_BUDGET] ?: "0" }
    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.NOTIFICATION_ENABLED] ?: true }
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.DARK_MODE] ?: false }
    val syncEnabled: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.SYNC_ENABLED] ?: false }

    suspend fun saveUserName(name: String, syncNow: Boolean = true) =
        savePreference(PreferencesKeys.USER_NAME, name, syncNow)

    suspend fun saveCurrency(currency: String, syncNow: Boolean = true) =
        savePreference(PreferencesKeys.CURRENCY, currency, syncNow)

    suspend fun saveMonthlyBudget(budget: String, syncNow: Boolean = true): Result<Unit> {
        val budgetValue = budget.toDoubleOrNull()
        if (budgetValue == null) {
            Log.w(TAG, "Invalid budget value: $budget. Cannot convert to Double.")
            return Result.failure(IllegalArgumentException("Invalid budget format: $budget"))
        }

        if (budgetValue < 0) {
            Log.w(TAG, "Negative budget value: $budget")
            return Result.failure(IllegalArgumentException("Budget cannot be negative"))
        }

        return savePreference(PreferencesKeys.MONTHLY_BUDGET, budget, syncNow)
    }

    suspend fun saveNotificationEnabled(enabled: Boolean, syncNow: Boolean = true) =
        savePreference(PreferencesKeys.NOTIFICATION_ENABLED, enabled, syncNow)

    suspend fun saveDarkMode(enabled: Boolean, syncNow: Boolean = true) =
        savePreference(PreferencesKeys.DARK_MODE, enabled, syncNow)

    suspend fun saveSyncEnabled(enabled: Boolean, syncNow: Boolean = true) =
        savePreference(PreferencesKeys.SYNC_ENABLED, enabled, syncNow)

    suspend fun completeOnboarding(syncNow: Boolean = true) =
        savePreference(PreferencesKeys.ONBOARDING_COMPLETED, true, syncNow)

    suspend fun resetOnboarding(syncNow: Boolean = true) =
        savePreference(PreferencesKeys.ONBOARDING_COMPLETED, false, syncNow)

    private suspend fun <T> savePreference(
        key: Preferences.Key<T>,
        value: T,
        syncNow: Boolean = true
    ): Result<Unit> {
        return try {
            context.dataStore.edit { it[key] = value }

            if (syncNow) {
                syncToFirestore()
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save preference ${key.name}", e)
            Result.failure(e)
        }
    }

    suspend fun batchSave(block: suspend UserPreferencesRepository.() -> Unit): Result<Unit> {
        return try {
            block()
            syncToFirestore()
        } catch (e: Exception) {
            Log.e(TAG, "Batch save failed", e)
            Result.failure(e)
        }
    }

       suspend fun syncToFirestore(): Result<Unit> {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            Log.w(TAG, "Cannot sync to Firestore: user not authenticated")
            return Result.failure(IllegalStateException("User not authenticated"))
        }

        return try {
            val preferences = context.dataStore.data.first()

            val budgetString = preferences[PreferencesKeys.MONTHLY_BUDGET] ?: "0"
            val budgetValue = budgetString.toDoubleOrNull()

            if (budgetValue == null) {
                Log.e(TAG, "Budget conversion failed for value: $budgetString. Defaulting to 0.0")
            }

            val userPreference = FirestoreUserPreferences(
                userId = userId,
                userName = preferences[PreferencesKeys.USER_NAME] ?: "",
                currency = preferences[PreferencesKeys.CURRENCY] ?: "KES",
                monthlyBudget = budgetValue ?: 0.0,
                notificationEnabled = preferences[PreferencesKeys.NOTIFICATION_ENABLED] ?: true,
                onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
                darkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
                syncEnabled = preferences[PreferencesKeys.SYNC_ENABLED] ?: false,
                lastUpdated = System.currentTimeMillis()
            )

            firestore.collection("user_preferences")
                .document(userId)
                .set(userPreference)
                .await()

            Log.d(TAG, "Successfully synced preferences to Firestore for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync preferences to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun loadFromFirestore(): Result<Unit> {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            Log.w(TAG, "Cannot load from Firestore: user not authenticated")
            return Result.failure(IllegalStateException("User not authenticated"))
        }

        return try {
            val document = firestore.collection("user_preferences")
                .document(userId)
                .get()
                .await()

            if (!document.exists()) {
                Log.i(TAG, "No preferences found in Firestore for user: $userId")
                return Result.failure(NoSuchElementException("No preferences found in Firestore"))
            }

            val pref = document.toObject(FirestoreUserPreferences::class.java)

            if (pref == null) {
                Log.e(TAG, "Failed to deserialize preferences from Firestore")
                return Result.failure(IllegalStateException("Failed to parse Firestore preferences"))
            }

            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.USER_NAME] = pref.userName
                preferences[PreferencesKeys.CURRENCY] = pref.currency
                preferences[PreferencesKeys.MONTHLY_BUDGET] = pref.monthlyBudget.toString()
                preferences[PreferencesKeys.NOTIFICATION_ENABLED] = pref.notificationEnabled
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] = pref.onboardingCompleted
                preferences[PreferencesKeys.DARK_MODE] = pref.darkMode
                preferences[PreferencesKeys.SYNC_ENABLED] = pref.syncEnabled
            }

            Log.d(TAG, "Successfully loaded preferences from Firestore for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load preferences from Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun getCurrentPreferences(): UserPreferences {
        val preferences = context.dataStore.data.first()
        return UserPreferences(
            userName = preferences[PreferencesKeys.USER_NAME] ?: "",
            currency = preferences[PreferencesKeys.CURRENCY] ?: "KES",
            monthlyBudget = preferences[PreferencesKeys.MONTHLY_BUDGET] ?: "0",
            notificationEnabled = preferences[PreferencesKeys.NOTIFICATION_ENABLED] ?: true,
            onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            darkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
            syncEnabled = preferences[PreferencesKeys.SYNC_ENABLED] ?: false
        )
    }
}

data class UserPreferences(
    val userName: String,
    val currency: String,
    val monthlyBudget: String,
    val notificationEnabled: Boolean,
    val onboardingCompleted: Boolean,
    val darkMode: Boolean,
    val syncEnabled: Boolean
)