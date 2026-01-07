package ke.ac.ku.ledgerly.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.ac.ku.ledgerly.data.repository.BillReminderRepository
import ke.ac.ku.ledgerly.data.service.NotificationService
import ke.ac.ku.ledgerly.utils.FormatingUtils.formatCurrency
import ke.ac.ku.ledgerly.utils.sendOverdueBillNotification
import java.util.concurrent.TimeUnit

@HiltWorker
class BillReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val billReminderRepository: BillReminderRepository,
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
        val billsNeedingReminder = billReminderRepository.getBillsNeedingReminder()

        if (billsNeedingReminder.isEmpty()) {
            return
        }

        billsNeedingReminder.forEach { bill ->
            val daysUntilDue = bill.daysUntilDue
            val billId = bill.id ?: return@forEach
            val dueDate = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(bill.dueDate))

            val sent = if (daysUntilDue < 0) {
                notificationService.sendOverdueBillNotification(
                    billId = billId,
                    billName = bill.billName,
                    amount = formatCurrency(bill.amount),
                    daysPastDue = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - bill.dueDate).toInt()

                )
                true
            } else {
                notificationService.sendBillReminderNotification(
                    billId = billId,
                    billName = bill.billName,
                    amount = formatCurrency(bill.amount),
                    dueDate = dueDate,
                    daysUntilDue = daysUntilDue
                )
            }

            // Mark reminder as sent if notification was successful
            if (sent) {
                billReminderRepository.markReminderSent(billId)
            }
        }
    }

    private fun isRecoverable(exception: Exception): Boolean {
        return !exception.javaClass.simpleName.contains("PersistenceException")
    }
}