package ke.ac.ku.ledgerly.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.repository.DebtRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class DebtReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val debtRepository: DebtRepository
) : CoroutineWorker(appContext, params) {

    companion object {
        const val CHANNEL_ID = "debt_reminders"
        const val NOTIFICATION_ID_BASE = 1000
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
        val debtsNeedingReminder = debtRepository.getDebtsNeedingReminder()

        if (debtsNeedingReminder.isEmpty()) {
            return
        }

        createNotificationChannel()

        debtsNeedingReminder.forEach { debt ->
            val daysUntilDue =
                ((debt.dueDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()

            val title = if (debt.debtType == "owe") {
                "Debt Reminder: Payment Due Soon"
            } else {
                "Collection Reminder: Payment Due Soon"
            }

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dueDate = dateFormat.format(Date(debt.dueDate))

            val message = "${debt.personName} - ${debt.amount} (Due: $dueDate)"

            val debtId = debt.id ?: return@forEach

            sendNotification(
                title = title,
                message = message,
                notificationId = NOTIFICATION_ID_BASE + debtId.toInt(),
                daysUntilDue = daysUntilDue,
                debtId = debtId
            )
        }
    }

    private fun sendNotification(
        title: String,
        message: String,
        notificationId: Int,
        daysUntilDue: Int,
        debtId: Long
    ) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 13+ runtime permission check
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val hasPermission =
                androidx.core.content.ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) return
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_ledgerly)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    when {
                        daysUntilDue < 0 -> "This debt is OVERDUE!"
                        daysUntilDue == 0 -> "This debt is due TODAY!"
                        else -> "This debt is due in $daysUntilDue days."
                    }
                )
            )
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        val name = "Debt Reminders"
        val descriptionText = "Reminders for upcoming debt payments"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun isRecoverable(e: Exception): Boolean {
        return when (e) {
            is java.io.IOException -> true        // transient I/O issues
            is android.database.sqlite.SQLiteException -> false // DB corruption, schema issues
            is IllegalArgumentException -> false  // invalid data
            else -> false
        }
    }

}
