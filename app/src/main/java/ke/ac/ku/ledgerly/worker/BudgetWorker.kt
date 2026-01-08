package ke.ac.ku.ledgerly.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.ac.ku.ledgerly.data.repository.BudgetRepository
import ke.ac.ku.ledgerly.data.repository.NotificationRepository
import ke.ac.ku.ledgerly.service.NotificationService
import ke.ac.ku.ledgerly.utils.FormatingUtils
import ke.ac.ku.ledgerly.utils.sendBudgetWarning

@HiltWorker
class BudgetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val budgetRepository: BudgetRepository,
    private val notificationService: NotificationService,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val budgetsExceedingThreshold = budgetRepository.getBudgetsExceedingThreshold(80)
            budgetsExceedingThreshold.forEach { budget ->
                notificationService.sendBudgetWarning(
                    notificationRepository = notificationRepository,
                    budgetId = budget.category + budget.monthYear,
                    category = budget.category,
                    percentageUsed = budget.percentageUsed.toDouble(),
                    spent = FormatingUtils.formatToDecimalValue(budget.currentSpending.toDouble()),
                    limit = FormatingUtils.formatToDecimalValue(budget.monthlyBudget.toDouble())
                )
            }
           Result.success()
        } catch (e: Exception) {
            Log.e("BudgetWorker", "Failed to check budgets", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
