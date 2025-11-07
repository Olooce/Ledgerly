package ke.ac.ku.ledgerly

import android.content.Context
import android.util.Log
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.ac.ku.ledgerly.worker.RecurringTransactionWorker
import ke.ac.ku.ledgerly.worker.SyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerSetup @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val TAG = "WorkManagerSetup"
        private const val SYNC_WORK_NAME = "full_sync_work"
        private const val RECURRING_WORK_NAME = "recurring_transactions_work"
        private const val MIN_INTERVAL_MINUTES = 15L
    }

    fun setupRecurringTransactionWork() {
        Log.d(TAG, "Setting up recurring transaction work")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val recurringWorkRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setInitialDelay(30, TimeUnit.SECONDS)
            .addTag(RECURRING_WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            RECURRING_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            recurringWorkRequest
        )

        Log.d(TAG, "Recurring transaction work scheduled")
    }

    fun cancelRecurringTransactionWork() {
        Log.d(TAG, "Cancelling recurring transaction work")
        workManager.cancelUniqueWork(RECURRING_WORK_NAME)
    }

    fun schedulePeriodicSync(
        repeatIntervalHours: Long = 6,
        requireWifi: Boolean = true,
        requireCharging: Boolean = false
    ) {
        val intervalHours = maxOf(1, repeatIntervalHours)

        Log.d(TAG, "Scheduling periodic sync: interval=${intervalHours}h, wifi=$requireWifi, charging=$requireCharging")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (requireWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .setRequiresCharging(requireCharging)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<SyncWorker>(
            intervalHours, TimeUnit.HOURS,
            MIN_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(10, TimeUnit.SECONDS)
            .addTag(SYNC_WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWork
        )

        Log.d(TAG, "Periodic sync scheduled successfully")
    }

    fun cancelPeriodicSync() {
        Log.d(TAG, "Cancelling periodic sync")
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
    }

    fun cancelAllWork() {
        Log.d(TAG, "Cancelling all work")
        cancelPeriodicSync()
        cancelRecurringTransactionWork()
    }

    fun getSyncWorkInfo(): Flow<WorkInfo?> =
        workManager.getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map { it.firstOrNull() }

    fun getRecurringWorkInfo(): Flow<WorkInfo?> =
        workManager.getWorkInfosForUniqueWorkFlow(RECURRING_WORK_NAME)
            .map { it.firstOrNull() }

    fun isSyncWorkRunningFlow(): Flow<Boolean> =
        getSyncWorkInfo().map { it?.state == WorkInfo.State.RUNNING }

    fun isSyncWorkEnqueuedFlow(): Flow<Boolean> =
        getSyncWorkInfo().map { it?.state == WorkInfo.State.ENQUEUED }

    fun pruneWork() {
        Log.d(TAG, "Pruning completed work")
        workManager.pruneWork()
    }
}
