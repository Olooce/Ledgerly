package ke.ac.ku.ledgerly

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.ac.ku.ledgerly.worker.RecurringTransactionWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerSetup @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun setupRecurringTransactionWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val recurringWorkRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
            1, TimeUnit.DAYS // Run once per day
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS)
            .addTag("recurring_transactions")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "recurring_transactions_work",
            ExistingPeriodicWorkPolicy.KEEP,
            recurringWorkRequest
        )
    }

    fun cancelRecurringTransactionWork() {
        WorkManager.getInstance(context)
            .cancelUniqueWork("recurring_transactions_work")
    }
}