package ke.ac.ku.ledgerly.data.model

sealed class NotificationType(val value: String) {
    object Bill : NotificationType("bill_reminder")
    object Debt : NotificationType("debt_reminder")
    object Budget : NotificationType("budget_alert")
    object Savings : NotificationType("savings_goal")
    object General : NotificationType("general")
}