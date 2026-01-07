package ke.ac.ku.ledgerly.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.ac.ku.ledgerly.data.repository.BudgetRepository
import ke.ac.ku.ledgerly.data.service.NotificationService
import ke.ac.ku.ledgerly.utils.FormatingUtils
import ke.ac.ku.ledgerly.utils.sendBudgetWarning

@HiltWorker
class BudgetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val budgetRepository: BudgetRepository,
    private val notificationService: NotificationService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val budgetsExceedingThreshold = budgetRepository.getBudgetsExceedingThreshold(80)
            budgetsExceedingThreshold.forEach { budget ->
                notificationService.sendBudgetWarning(
                    budgetId = budget.category + budget.monthYear,
                    category = budget.category,
                    percentageUsed = budget.percentageUsed,
                    spent = FormatingUtils.formatCurrency(budget.currentSpending),
                    limit = FormatingUtils.formatCurrency(budget.monthlyBudget)
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}