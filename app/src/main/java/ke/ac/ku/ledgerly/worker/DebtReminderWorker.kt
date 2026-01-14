package ke.ac.ku.ledgerly.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.ac.ku.ledgerly.data.repository.DebtRepository
import ke.ac.ku.ledgerly.service.NotificationService
import ke.ac.ku.ledgerly.utils.CurrencyFormatter.formatCurrency
import ke.ac.ku.ledgerly.utils.sendOverdueDebtNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@HiltWorker
class DebtReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val debtRepository: DebtRepository,
    private val notificationService: NotificationService
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            checkAndSendReminders()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3 && isRecoverable(e)) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun checkAndSendReminders() {
        val debtsNeedingReminder = debtRepository.getDebtsNeedingReminder()

        if (debtsNeedingReminder.isEmpty()) {
            return
        }

        debtsNeedingReminder.forEach { debt ->
            val daysUntilDue =
                ((debt.dueDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()

            val debtId = debt.id ?: return@forEach

            val dueDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(debt.dueDate))

            val sent = if (daysUntilDue < 0) {
                notificationService.sendOverdueDebtNotification(
                    debtId = debtId,
                    personName = debt.personName,
                    amount = formatCurrency(debt.amount),
                    daysPastDue = TimeUnit.MILLISECONDS
                        .toDays(System.currentTimeMillis() - debt.dueDate)
                        .toInt(),
                    debtType = debt.debtType
                )
            } else {
                notificationService.sendDebtReminderNotification(
                    debtId = debtId,
                    personName = debt.personName,
                    amount = formatCurrency(debt.amount),
                    dueDate = dueDate,
                    debtType = debt.debtType,
                    daysUntilDue = daysUntilDue
                )
            }

            if (sent) {
                debtRepository.markReminderSent(debtId)
            }
        }
    }

    private fun isRecoverable(e: Exception): Boolean {
        return when (e) {
            is java.io.IOException -> true
            is android.database.sqlite.SQLiteException -> false
            is IllegalArgumentException -> false
            else -> false
        }
    }
}
