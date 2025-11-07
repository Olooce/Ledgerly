package ke.ac.ku.ledgerly.domain

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.asFlow
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import ke.ac.ku.ledgerly.auth.data.AuthRepository
import ke.ac.ku.ledgerly.data.repository.SyncRepository
import ke.ac.ku.ledgerly.data.repository.SyncResult
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Singleton
class SyncManager @Inject constructor(
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) {

    val syncWorkInfoFlow: Flow<WorkInfo?> = WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData("full_sync")
        .asFlow()
        .map { workInfos -> workInfos.firstOrNull() }

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "SyncManager"
        private const val PREFS_NAME = "sync_prefs"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_LAST_SYNC = "last_sync"
    }

    private fun getDeviceId(): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DEVICE_ID, null) ?: run {
            val newDeviceId = UUID.randomUUID().toString()
            prefs.edit { putString(KEY_DEVICE_ID, newDeviceId) }
            newDeviceId
        }
    }

    suspend fun syncAllData(
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        try {
            if (!authRepository.isUserAuthenticated()) {
                withContext(Dispatchers.Main) {
                    onError?.invoke("Please sign in to enable cloud sync")
                }
                return
            }

            val deviceId = getDeviceId()

            // run heavy tasks on IO thread
            val result = withContext(Dispatchers.IO) {
                syncRepository.fullSync(deviceId)
            }

            val prefResult = withContext(Dispatchers.IO) {
                userPreferencesRepository.syncToFirestore()
            }

            if (result.isSuccessful && prefResult.isSuccess) {
                updateLastSyncTime()
                withContext(Dispatchers.Main.immediate) { onSuccess?.invoke() }
                Log.d(TAG, "Full sync completed successfully")
            } else {
                val errorMessage = buildString {
                    append("Sync failed: ")
                    if (result.transactions is SyncResult.Error)
                        append("Transactions - ${result.transactions.message}. ")
                    if (result.budgets is SyncResult.Error)
                        append("Budgets - ${result.budgets.message}. ")
                    if (result.recurringTransactions is SyncResult.Error)
                        append("Recurring - ${result.recurringTransactions.message}. ")
                    if (prefResult.isFailure)
                        append("Preferences - ${prefResult.exceptionOrNull()?.message}.")
                }.trim()

                Log.e(TAG, errorMessage)
                withContext(Dispatchers.Main.immediate) { onError?.invoke(errorMessage) }
            }
        } catch (e: Exception) {
            val errorMsg = "Sync failed: ${e.message}"
            Log.e(TAG, errorMsg, e)
            withContext(Dispatchers.Main.immediate) { onError?.invoke(errorMsg) }
            throw e
        }
    }


    suspend fun isCloudSyncEnabled(): Boolean {
        return userPreferencesRepository.syncEnabled.first()
    }
    fun setCloudSyncEnabled(enabled: Boolean) {
        scope.launch {
            if (enabled && !authRepository.isUserAuthenticated()) {
                Log.w(TAG, "Cannot enable cloud sync while user is signed out")
                return@launch
            }

            if (enabled) {
                syncAllData(
                    onSuccess = { Log.d(TAG, "Manual sync completed successfully") },
                    onError = { e ->
                        Log.e(TAG, "Manual sync failed: $e")
                    }
                )
            }
        }
    }

    fun getLastSyncTime(): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_SYNC, 0)
    }

    private fun updateLastSyncTime() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putLong(KEY_LAST_SYNC, System.currentTimeMillis()) }
    }
}


