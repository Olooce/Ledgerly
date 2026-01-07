package ke.ac.ku.ledgerly.data.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.ac.ku.ledgerly.MainActivity
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.model.NotificationEntity
import ke.ac.ku.ledgerly.data.model.NotificationType
import ke.ac.ku.ledgerly.data.repository.NotificationRepository
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository
) {
    companion object {
        private const val NOTIFICATION_ID_BILLS = 2000
        private const val NOTIFICATION_ID_DEBTS = 1000
        private const val NOTIFICATION_ID_BUDGET = 3000
        private const val NOTIFICATION_ID_SAVINGS = 4000
        private const val NOTIFICATION_ID_GENERAL = 5000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                NotificationType.Bill.value,
                "Bill Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming bills and subscriptions"
                enableVibration(true)
                enableLights(true)
            },
            NotificationChannel(
                NotificationType.Debt.value,
                "Debt Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming debt payments"
            },
            NotificationChannel(
                NotificationType.Budget.value,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when you're approaching budget limits"
            },
            NotificationChannel(
                NotificationType.Savings.value,
                "Savings Goals",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Updates on your savings goals progress"
            },
            NotificationChannel(
                NotificationType.General.value,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    suspend fun sendBillReminderNotification(
        billId: Long,
        billName: String,
        amount: String,
        dueDate: String,
        daysUntilDue: Int
    ): Boolean {
        val title = "Bill Reminder: $billName"
        val message = when {
            daysUntilDue < 0 -> "$billName - $amount (OVERDUE)"
            daysUntilDue == 0 -> "$billName - $amount (DUE TODAY)"
            else -> "$billName - $amount (Due: $dueDate)"
        }
        val notificationEntity = NotificationEntity(
            type = NotificationType.Bill.value,
            title = title,
            message = message,
            relatedId = billId.toString(),
            relatedType = "BillReminder",
            actionUrl = "ledgerly://billReminders/$billId"
        )
        notificationRepository.insertNotification(notificationEntity)

        if (!hasNotificationPermission()) {
            return true // Still return true since DB entry was created
        }

        val urgencyColor = when {
            daysUntilDue < 0 -> context.getColor(android.R.color.holo_red_dark)
            daysUntilDue == 0 -> context.getColor(android.R.color.holo_orange_dark)
            else -> context.getColor(android.R.color.holo_blue_bright)
        }

        return sendSystemNotification(
            channelId = NotificationType.Bill.value,
            notificationId = NOTIFICATION_ID_BILLS + (billId % Int.MAX_VALUE).toInt(),
            title = title,
            message = message,
            bigText = message,
            color = urgencyColor,
            priority = NotificationCompat.PRIORITY_HIGH,
            extras = mapOf("billId" to billId)
        )
    }

    suspend fun sendDebtReminderNotification(
        debtId: Long,
        personName: String,
        amount: String,
        dueDate: String,
        debtType: String,
        daysUntilDue: Int
    ): Boolean {
        val title = if (debtType == "owe") {
            "Debt Reminder: Payment Due Soon"
        } else {
            "Collection Reminder: Payment Due Soon"
        }

        val message = "$personName - $amount (Due: $dueDate)"
        val bigText = when {
            daysUntilDue < 0 -> "This debt is OVERDUE!"
            daysUntilDue == 0 -> "This debt is due TODAY!"
            else -> "This debt is due in $daysUntilDue days."
        }

        val notificationEntity = NotificationEntity(
            type = NotificationType.Debt.value,
            title = title,
            message = message,
            relatedId = debtId.toString(),
            relatedType = "Debt",
            actionUrl = "ledgerly://debtTracker/$debtId"
        )
        notificationRepository.insertNotification(notificationEntity)


        if (!hasNotificationPermission()) {
            return true
        }

        return sendSystemNotification(
            channelId = NotificationType.Debt.value,
            notificationId = NOTIFICATION_ID_DEBTS + (debtId % Int.MAX_VALUE).toInt(),
            title = title,
            message = message,
            bigText = bigText,
            priority = NotificationCompat.PRIORITY_DEFAULT,
            extras = mapOf("debtId" to debtId)
        )
    }

    suspend fun sendBudgetAlertNotification(
        budgetId: String,
        category: String,
        percentageUsed: Double,
        spent: String,
        limit: String
    ): Boolean {
        val title = "Budget Alert: $category"
        val message = "You've spent ${String.format("%.0f", percentageUsed)}% of your budget"
        val bigText = "You've spent $spent out of $limit for $category this month."
        val key = budgetNotificationKey(category)

        val notificationEntity =  NotificationEntity(
            type = NotificationType.Budget.value,
            title = title,
            message = message,
            relatedId = key,
            relatedType = "Budget",
            actionUrl = "ledgerly://budget/$budgetId"
        )

        notificationRepository.insertNotification(notificationEntity)

        if (!hasNotificationPermission()) {
            return true
        }

        return sendSystemNotification(
            channelId = NotificationType.Budget.value,
            notificationId = NOTIFICATION_ID_BUDGET + budgetId.hashCode(),
            title = title,
            message = message,
            bigText = bigText,
            color = context.getColor(android.R.color.holo_orange_dark),
            priority = NotificationCompat.PRIORITY_DEFAULT,
            extras = mapOf("budgetId" to budgetId)
        )
    }

    fun budgetNotificationKey(category: String): String {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        return "$category|$month|$year"
    }


    suspend fun sendSavingsGoalNotification(
        goalId: Long,
        goalName: String,
        percentageComplete: Double,
        currentAmount: String,
        targetAmount: String
    ): Boolean {
        val title = "Savings Goal Update: $goalName"
        val message = "You've reached ${String.format("%.0f", percentageComplete)}% of your goal"
        val bigText = "You've saved $currentAmount out of $targetAmount for $goalName!"

        val notificationEntity = NotificationEntity(
            type = NotificationType.Savings.value,
            title = title,
            message = message,
            relatedId = goalId.toString(),
            relatedType = "SavingsGoal",
            actionUrl = "ledgerly://savingsGoals/$goalId"
        )
        notificationRepository.insertNotification(notificationEntity)

        if (!hasNotificationPermission()) {
            return true
        }

        return sendSystemNotification(
            channelId = NotificationType.Savings.value,
            notificationId = NOTIFICATION_ID_SAVINGS +
                    (goalId % Int.MAX_VALUE).toInt(),
            title = title,
            message = message,
            bigText = bigText,
            color = context.getColor(android.R.color.holo_green_dark),
            priority = NotificationCompat.PRIORITY_LOW,
            extras = mapOf("goalId" to goalId)
        )
    }

    suspend fun sendGeneralNotification(
        title: String,
        message: String,
        bigText: String? = null
    ): Boolean {
        val notificationEntity = NotificationEntity(
            type = NotificationType.General.value,
            title = title,
            message = message,
            relatedId = null,
            relatedType = null,
            actionUrl = null
        )
        notificationRepository.insertNotification(notificationEntity)

        if (!hasNotificationPermission()) {
            return true
        }

        return sendSystemNotification(
            channelId = NotificationType.General.value,
            notificationId = NOTIFICATION_ID_GENERAL + (System.currentTimeMillis() % 10000).toInt(),
            title = title,
            message = message,
            bigText = bigText ?: message,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    private fun sendSystemNotification(
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        bigText: String,
        color: Int = context.getColor(android.R.color.holo_blue_bright),
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        extras: Map<String, Any> = emptyMap()
    ): Boolean {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                extras.forEach { (key, value) ->
                    when (value) {
                        is Long -> putExtra(key, value)
                        is String -> putExtra(key, value)
                        is Int -> putExtra(key, value)
                    }
                }
            }


            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_ledgerly)
                .setColor(color)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(priority)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                .build()

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (hasNotificationPermission()) {
                notificationManager.notify(notificationId, notification)
                return true
            }

            return false
        } catch (e: Exception) {
            Log.e("NotificationService", "Failed to send notification", e)
            return false
        }
    }

}
