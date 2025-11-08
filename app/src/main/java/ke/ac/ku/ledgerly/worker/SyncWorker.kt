package ke.ac.ku.ledgerly.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.ac.ku.ledgerly.domain.SyncManager

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            syncManager.syncAllData(
                onSuccess = { Log.d(TAG, "Worker sync completed successfully") },
                onError = { Log.e(TAG, "Worker sync failed: $it") }
            )
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker sync exception", e)
            Result.retry()
        }
    }


}
