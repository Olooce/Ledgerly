package ke.ac.ku.ledgerly.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
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
        val SYNC_WIFI_ONLY = booleanPreferencesKey("sync_wifi_only")
        val SYNC_CHARGING_ONLY = booleanPreferencesKey("sync_charging_only")
        val SYNC_INTERVAL = longPreferencesKey("sync_interval")
        val LAST_UPDATED = longPreferencesKey("last_updated")
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
    val syncWifiOnly: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.SYNC_WIFI_ONLY] ?: true }
    val syncChargingOnly: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKeys.SYNC_CHARGING_ONLY] ?: false }
    val syncInterval: Flow<Long> = context.dataStore.data.map { it[PreferencesKeys.SYNC_INTERVAL] ?: 6L }

    suspend fun saveUserName(name: String, syncNow: Boolean = true) =
        savePreference(PreferencesKeys.USER_NAME, name, syncNow)

    suspend fun saveCurrency(currency: String, syncNow: Boolean = true) =
        savePreference(PreferencesKeys.CURRENCY, currency, syncNow)

    suspend fun saveMonthlyBudget(budget: String?, syncNow: Boolean = true): Result<Unit> {
        if (budget.isNullOrBlank()) {
            Log.i(TAG, "No budget provided — skipping save.")
            return Result.success(Unit)
        }
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

    suspend fun saveSyncWifiOnly(wifiOnly: Boolean, syncNow: Boolean = true) =
        savePreference(PreferencesKeys.SYNC_WIFI_ONLY, wifiOnly, syncNow)

    suspend fun saveSyncChargingOnly(chargingOnly: Boolean, syncNow: Boolean = true) =
        savePreference(PreferencesKeys.SYNC_CHARGING_ONLY, chargingOnly, syncNow)

    suspend fun saveSyncInterval(hours: Long, syncNow: Boolean = true) =
        savePreference(PreferencesKeys.SYNC_INTERVAL, hours, syncNow)

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
            val now = System.currentTimeMillis()
            context.dataStore.edit { prefs ->
                prefs[key] = value
                prefs[PreferencesKeys.LAST_UPDATED] = now
            }
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

    suspend fun updatePreferences(
        userName: String? = null,
        currency: String? = null,
        monthlyBudget: String? = null,
        notificationEnabled: Boolean? = null,
        onboardingCompleted: Boolean? = null,
        darkMode: Boolean? = null,
        syncEnabled: Boolean? = null,
        syncWifiOnly: Boolean? = null,
        syncChargingOnly: Boolean? = null,
        syncInterval: Long? = null,
        lastUpdated: Long = System.currentTimeMillis(),
        syncNow: Boolean = true
    ): Result<Unit> {
        return try {
            context.dataStore.edit { prefs ->
                userName?.let { prefs[PreferencesKeys.USER_NAME] = it }
                currency?.let { prefs[PreferencesKeys.CURRENCY] = it }
                monthlyBudget?.let { prefs[PreferencesKeys.MONTHLY_BUDGET] = it }
                notificationEnabled?.let { prefs[PreferencesKeys.NOTIFICATION_ENABLED] = it }
                onboardingCompleted?.let { prefs[PreferencesKeys.ONBOARDING_COMPLETED] = it }
                darkMode?.let { prefs[PreferencesKeys.DARK_MODE] = it }
                syncEnabled?.let { prefs[PreferencesKeys.SYNC_ENABLED] = it }
                syncWifiOnly?.let { prefs[PreferencesKeys.SYNC_WIFI_ONLY] = it }
                syncChargingOnly?.let { prefs[PreferencesKeys.SYNC_CHARGING_ONLY] = it }
                syncInterval?.let { prefs[PreferencesKeys.SYNC_INTERVAL] = it }
                prefs[PreferencesKeys.LAST_UPDATED] = lastUpdated
            }
            if (syncNow) {
                syncToFirestore()
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update preferences", e)
            Result.failure(e)
        }
    }

    suspend fun batchSave(block: suspend UserPreferencesRepository.() -> List<Result<Unit>>): Result<Unit> {
        return try {
            val results = block()
            val firstFailure = results.firstOrNull { it.isFailure }
            if (firstFailure != null) {
                Log.e(TAG, "Batch save aborted due to failure: ${firstFailure.exceptionOrNull()}")
                return firstFailure
            }
            syncToFirestore()
        } catch (e: Exception) {
            Log.e(TAG, "Batch save failed", e)
            Result.failure(e)
        }
    }

    suspend fun loadFromFirestore(): Result<Unit> {
        return try {
            val userId = authRepository.getCurrentUserId()
                ?: return Result.failure(Exception("Not authenticated"))
            val document = firestore.collection("user_preferences")
                .document(userId)
                .get()
                .await()
            if (!document.exists()) return Result.success(Unit)
            val remotePrefs = document.toObject<FirestoreUserPreferences>() ?: return Result.success(Unit)
            val localPrefs = getCurrentPreferences()

            val updateResult = if (remotePrefs.lastUpdated > localPrefs.lastUpdated) {
                updatePreferences(
                    userName = remotePrefs.userName,
                    currency = remotePrefs.currency,
                    monthlyBudget = remotePrefs.monthlyBudget.toString(),
                    notificationEnabled = remotePrefs.notificationEnabled,
                    onboardingCompleted = remotePrefs.onboardingCompleted,
                    darkMode = remotePrefs.darkMode,
                    syncEnabled = remotePrefs.syncEnabled,
                    syncWifiOnly = remotePrefs.syncWifiOnly,
                    syncChargingOnly = remotePrefs.syncChargingOnly,
                    syncInterval = remotePrefs.syncInterval,
                    lastUpdated = remotePrefs.lastUpdated,
                    syncNow = false
                )
            } else {
                Result.success(Unit)
            }
            updateResult
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load preferences from Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun syncToFirestore(): Result<Unit> {
        return try {
            val userId = authRepository.getCurrentUserId() ?: return Result.failure(Exception("Not authenticated"))
            val localPrefs = getCurrentPreferences()
            val firestorePrefs = FirestoreUserPreferences(
                userId = userId,
                userName = localPrefs.userName,
                currency = localPrefs.currency,
                monthlyBudget = localPrefs.monthlyBudget.toDoubleOrNull() ?: 0.0,
                notificationEnabled = localPrefs.notificationEnabled,
                onboardingCompleted = localPrefs.onboardingCompleted,
                darkMode = localPrefs.darkMode,
                syncEnabled = localPrefs.syncEnabled,
                syncWifiOnly = localPrefs.syncWifiOnly,
                syncChargingOnly = localPrefs.syncChargingOnly,
                syncInterval = localPrefs.syncInterval,
                lastUpdated = localPrefs.lastUpdated
            )
            firestore.collection("user_preferences")
                .document(userId)
                .set(firestorePrefs, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
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
            syncEnabled = preferences[PreferencesKeys.SYNC_ENABLED] ?: false,
            syncWifiOnly = preferences[PreferencesKeys.SYNC_WIFI_ONLY] ?: true,
            syncChargingOnly = preferences[PreferencesKeys.SYNC_CHARGING_ONLY] ?: false,
            syncInterval = preferences[PreferencesKeys.SYNC_INTERVAL] ?: 6L,
            lastUpdated = preferences[PreferencesKeys.LAST_UPDATED] ?: 0L
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
    val syncEnabled: Boolean,
    val syncWifiOnly: Boolean,
    val syncChargingOnly: Boolean,
    val syncInterval: Long,
    val lastUpdated: Long = 0L
)