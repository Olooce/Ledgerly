package ke.ac.ku.ledgerly.domain

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import ke.ac.ku.ledgerly.auth.data.AuthRepository
import ke.ac.ku.ledgerly.data.repository.SyncRepository
import ke.ac.ku.ledgerly.data.repository.SyncResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Singleton
class SyncManager @Inject constructor(
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "SyncManager"
        private const val PREFS_NAME = "sync_prefs"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_SYNC_ENABLED = "sync_enabled"
    }

    private fun getDeviceId(): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DEVICE_ID, null) ?: run {
            val newDeviceId = UUID.randomUUID().toString()
            prefs.edit { putString(KEY_DEVICE_ID, newDeviceId) }
            newDeviceId
        }
    }

    fun syncAllData(
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        scope.launch {
            try {
                if (!authRepository.isUserAuthenticated()) {
                    withContext(Dispatchers.Main) {
                        onError?.invoke("Please sign in to enable cloud sync")
                    }
                    return@launch
                }

                val deviceId = getDeviceId()
                val result = syncRepository.fullSync(deviceId)

                if (result.isSuccessful) {
                    updateLastSyncTime()
                    withContext(Dispatchers.Main) { onSuccess?.invoke() }
                    Log.d(TAG, "Sync completed successfully")
                } else {
                    val errorMessage = when {
                        result.transactions is SyncResult.Error ->
                            "Transaction sync failed: ${result.transactions.message}"
                        result.budgets is SyncResult.Error ->
                            "Budget sync failed: ${result.budgets.message}"
                        else -> "Sync failed"
                    }
                    withContext(Dispatchers.Main) { onError?.invoke(errorMessage) }
                    Log.e(TAG, errorMessage)
                }
            } catch (e: Exception) {
                val errorMsg = "Sync failed: ${e.message}"
                withContext(Dispatchers.Main) { onError?.invoke(errorMsg) }
                Log.e(TAG, errorMsg, e)
            }
        }
    }

    fun isCloudSyncEnabled(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SYNC_ENABLED, false)
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (enabled && !authRepository.isUserAuthenticated()) {
            Log.w(TAG, "Ignoring cloud sync enable while user is signed out")
            return
        }

        prefs.edit { putBoolean(KEY_SYNC_ENABLED, enabled) }

        if (enabled) {
            syncAllData(
                onSuccess = { Log.d(TAG, "Manual sync completed") },
                onError = { e ->
                    prefs.edit { putBoolean(KEY_SYNC_ENABLED, false) }
                    Log.e(TAG, "Manual sync failed: $e")
                }
            )
        }
    }

    suspend fun isSyncEnabled(): Boolean {
        return authRepository.authState.first()
    }

    fun getLastSyncTime(): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_SYNC, 0)
    }

    private fun updateLastSyncTime() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putLong(KEY_LAST_SYNC, System.currentTimeMillis()) }
    }

    fun syncUserPreferences(darkMode: Boolean, syncEnabled: Boolean) {
        scope.launch {
            try {
                syncRepository.syncUserPreferences(darkMode, syncEnabled)
                Log.d(TAG, "User preferences synced successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync preferences: ${e.message}", e)
            }
        }
    }
}
