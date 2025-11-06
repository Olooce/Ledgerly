package ke.ac.ku.ledgerly.data.repository

import android.content.Context
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

    val userName: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.USER_NAME] ?: "" }
    val currency: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.CURRENCY] ?: "KES" }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.ONBOARDING_COMPLETED] ?: false }
    val monthlyBudget: Flow<String> = context.dataStore.data.map { it[PreferencesKeys.MONTHLY_BUDGET] ?: "0" }
    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.NOTIFICATION_ENABLED] ?: true }
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.DARK_MODE] ?: false }
    val syncEnabled: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.SYNC_ENABLED] ?: false }

    suspend fun saveUserName(name: String) = savePreference(PreferencesKeys.USER_NAME, name)
    suspend fun saveCurrency(currency: String) = savePreference(PreferencesKeys.CURRENCY, currency)
    suspend fun saveMonthlyBudget(budget: String) = savePreference(PreferencesKeys.MONTHLY_BUDGET, budget)
    suspend fun saveNotificationEnabled(enabled: Boolean) = savePreference(PreferencesKeys.NOTIFICATION_ENABLED, enabled)
    suspend fun saveDarkMode(enabled: Boolean) = savePreference(PreferencesKeys.DARK_MODE, enabled)
    suspend fun saveSyncEnabled(enabled: Boolean) = savePreference(PreferencesKeys.SYNC_ENABLED, enabled)

    suspend fun completeOnboarding() = savePreference(PreferencesKeys.ONBOARDING_COMPLETED, true)
    suspend fun resetOnboarding() = savePreference(PreferencesKeys.ONBOARDING_COMPLETED, false)

    private suspend fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
        syncToFirestore()
    }

    private suspend fun syncToFirestore() {
        val userId = authRepository.getCurrentUserId() ?: return
        try {
            val preferences = context.dataStore.data.first()
            val userPreference = FirestoreUserPreferences(
                userId = userId,
                userName = preferences[PreferencesKeys.USER_NAME] ?: "",
                currency = preferences[PreferencesKeys.CURRENCY] ?: "KES",
                monthlyBudget = preferences[PreferencesKeys.MONTHLY_BUDGET]?.toDoubleOrNull() ?: 0.0,
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadFromFirestore() {
        val userId = authRepository.getCurrentUserId() ?: return
        try {
            val document = firestore.collection("user_preferences")
                .document(userId)
                .get()
                .await()

            val pref = document.toObject(FirestoreUserPreferences::class.java)
            pref?.let {
                context.dataStore.edit { preferences ->
                    preferences[PreferencesKeys.USER_NAME] = it.userName
                    preferences[PreferencesKeys.CURRENCY] = it.currency
                    preferences[PreferencesKeys.MONTHLY_BUDGET] = it.monthlyBudget.toString()
                    preferences[PreferencesKeys.NOTIFICATION_ENABLED] = it.notificationEnabled
                    preferences[PreferencesKeys.ONBOARDING_COMPLETED] = it.onboardingCompleted
                    preferences[PreferencesKeys.DARK_MODE] = it.darkMode
                    preferences[PreferencesKeys.SYNC_ENABLED] = it.syncEnabled
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
