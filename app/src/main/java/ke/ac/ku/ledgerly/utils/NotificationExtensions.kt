package ke.ac.ku.ledgerly.utils

import ke.ac.ku.ledgerly.data.repository.NotificationRepository
import ke.ac.ku.ledgerly.data.service.NotificationService
import java.util.Calendar

//Send budget warning when spending reaches threshold
suspend fun NotificationService.sendBudgetWarning(
    notificationRepository: NotificationRepository,
    budgetId: String,
    category: String,
    percentageUsed: Double,
    spent: String,
    limit: String
) {
    if (percentageUsed >= 80.0) {
        // Check if a notification has been sent this month
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val monthYear = "$month|$year"
        val key = "$budgetId|$monthYear"

        val existingNotifications = notificationRepository.getNotificationsByRelatedEntity(
            relatedId = key,
            relatedType = "Budget"
        )

        if (existingNotifications.isEmpty()) {
            sendBudgetAlertNotification(
                budgetId = budgetId,
                category = category,
                percentageUsed = percentageUsed,
                spent = spent,
                limit = limit
            )
        }
    }
}

// Send savings milestone notification

suspend fun NotificationService.sendSavingsMilestone(
    goalId: Long,
    goalName: String,
    percentageComplete: Double,
    currentAmount: String,
    targetAmount: String
) {
    val milestones = listOf(25.0, 50.0, 75.0, 100.0)

    if (milestones.any { kotlin.math.abs(percentageComplete - it) < 1.0 }) {
        sendSavingsGoalNotification(
            goalId = goalId,
            goalName = goalName,
            percentageComplete = percentageComplete,
            currentAmount = currentAmount,
            targetAmount = targetAmount
        )
    }
}

//Send overdue debt notification
suspend fun NotificationService.sendOverdueDebtNotification(
    debtId: Long,
    personName: String,
    amount: String,
    daysPastDue: Int,
    debtType: String
) {
    val dueDate = "OVERDUE by $daysPastDue days"
    sendDebtReminderNotification(
        debtId = debtId,
        personName = personName,
        amount = amount,
        dueDate = dueDate,
        debtType = debtType,
        daysUntilDue = -daysPastDue
    )
}

//Send overdue bill notification

suspend fun NotificationService.sendOverdueBillNotification(
    billId: Long,
    billName: String,
    amount: String,
    daysPastDue: Int
) : Boolean {
    val dueDate = "OVERDUE by $daysPastDue days"
    return try {
    sendBillReminderNotification(
        billId = billId,
        billName = billName,
        amount = amount,
        dueDate = dueDate,
        daysUntilDue = -daysPastDue
    )
        true
    } catch (e: Exception) {
        false
    }
}

//Send welcome notification for new users

suspend fun NotificationService.sendWelcomeNotification(userName: String) {
    sendGeneralNotification(
        title = "Welcome to Ledgerly, $userName!",
        message = "Start tracking your finances and reach your financial goals.",
        bigText = "We're excited to help you manage your money better. Start by adding your first transaction!"
    )
}


// Send data export success notification

suspend fun NotificationService.sendExportSuccessNotification(fileName: String) {
    sendGeneralNotification(
        title = "Export Successful",
        message = "Your data has been exported to $fileName",
        bigText = "Your financial data has been successfully exported and is ready to share or backup."
    )
}

//Send recurring transaction notification
suspend fun NotificationService.sendRecurringTransactionNotification(
    transactionName: String,
    amount: String,
    type: String
) {
    val title = if (type == "income") "Recurring Income Recorded" else "Recurring Expense Recorded"
    sendGeneralNotification(
        title = title,
        message = "$transactionName - $amount has been automatically recorded.",
        bigText = "Your recurring transaction '$transactionName' for $amount has been automatically added to your ledger."
    )
}
