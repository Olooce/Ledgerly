package ke.ac.ku.ledgerly.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.ac.ku.ledgerly.MainActivity
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.repository.BillReminderRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class BillReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val billReminderRepository: BillReminderRepository
) : CoroutineWorker(appContext, params) {

    companion object {
        const val CHANNEL_ID = "bill_reminders"
        const val NOTIFICATION_ID_BASE = 2000
    }

    override suspend fun doWork(): Result {
        return try {
            checkAndSendReminders()
            Result.success()
        } catch (e: Exception) {
            // Retry only a limited number of times
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

        createNotificationChannel()

        billsNeedingReminder.forEach { bill ->
            val daysUntilDue = bill.daysUntilDue

            val title = "Bill Reminder: ${bill.billName}"

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dueDate = dateFormat.format(Date(bill.dueDate))

            val message = when {
                daysUntilDue < 0 -> "${bill.billName} - KES ${
                    String.format(
                        "%.2f",
                        bill.amount
                    )
                } (OVERDUE)"

                daysUntilDue == 0 -> "${bill.billName} - KES ${
                    String.format(
                        "%.2f",
                        bill.amount
                    )
                } (DUE TODAY)"

                else -> "${bill.billName} - KES ${
                    String.format(
                        "%.2f",
                        bill.amount
                    )
                } (Due: $dueDate)"
            }

            val billId = bill.id ?: return@forEach

            val sent = sendNotification(
                title = title,
                message = message,
                notificationId = NOTIFICATION_ID_BASE + (billId % Int.MAX_VALUE).toInt(),
                daysUntilDue = daysUntilDue,
                billId = billId
            )

            // Mark reminder as sent
            if (sent) {
                billReminderRepository.markReminderSent(billId)
            }
        }
    }

    private fun sendNotification(
        title: String,
        message: String,
        notificationId: Int,
        daysUntilDue: Int,
        billId: Long
    ): Boolean {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                return false
            }
        }


        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("billId", billId)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            billId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val urgencyColor = when {
            daysUntilDue < 0 -> applicationContext.getColor(android.R.color.holo_red_dark)
            daysUntilDue == 0 -> applicationContext.getColor(android.R.color.holo_orange_dark)
            else -> applicationContext.getColor(android.R.color.holo_blue_bright)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_ledgerly)
            .setColor(urgencyColor)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .build()

        notificationManager.notify(notificationId, notification)
        return true

    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Bill Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for upcoming bills and subscriptions"
            enableVibration(true)
            enableLights(true)
        }

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun isRecoverable(exception: Exception): Boolean {
        return !exception.javaClass.simpleName.contains("PersistenceException")
    }
}
