package ke.ac.ku.ledgerly.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.ac.ku.ledgerly.domain.CleanupManager

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val cleanupManager: CleanupManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            cleanupManager.cleanupOldDeletedItems()
            Result.success()
        } catch (e: Exception) {
            Log.e("CleanupWorker", "Cleanup failed", e)
            Result.retry()
        }
    }
}
