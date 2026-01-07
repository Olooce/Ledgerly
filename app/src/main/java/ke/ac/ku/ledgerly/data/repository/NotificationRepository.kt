package ke.ac.ku.ledgerly.data.repository

import ke.ac.ku.ledgerly.data.dao.NotificationDao
import ke.ac.ku.ledgerly.data.model.NotificationEntity
import ke.ac.ku.ledgerly.data.model.NotificationSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {

    suspend fun insertNotification(notification: NotificationEntity): Long {
        return notificationDao.insertNotification(notification)
    }

    suspend fun updateNotification(notification: NotificationEntity) {
        notificationDao.updateNotification(notification)
    }

    suspend fun deleteNotification(notification: NotificationEntity) {
        notificationDao.deleteNotification(notification)
    }

    suspend fun deleteNotificationById(id: Long) {
        notificationDao.deleteNotificationById(id)
    }

    suspend fun getNotificationById(id: Long): NotificationEntity? {
        return notificationDao.getNotificationById(id)
    }

    fun getAllNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getAllNotifications()
    }

    fun getUnreadNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.getUnreadNotifications()
    }

    fun getUnreadNotificationsLimited(limit: Int = 5): Flow<List<NotificationEntity>> {
        return notificationDao.getUnreadNotificationsLimited(limit)
    }

    fun getUnreadNotificationCount(): Flow<Int> {
        return notificationDao.getUnreadNotificationCount()
    }

    fun getNotificationSummary(): Flow<NotificationSummary> {
        return notificationDao.getNotificationSummary()
    }

    fun getNotificationsByType(type: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByType(type)
    }

    suspend fun getNotificationsByRelatedEntity(
        relatedId: Long,
        relatedType: String
    ): List<NotificationEntity> {
        return notificationDao.getNotificationsByRelatedEntity(relatedId, relatedType)
    }

    suspend fun markAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun softDeleteNotification(id: Long) {
        notificationDao.softDeleteNotification(id)
    }

    suspend fun softDeleteNotificationsByType(type: String) {
        notificationDao.softDeleteNotificationsByType(type)
    }

    suspend fun permanentlyDeleteMarkedNotifications() {
        notificationDao.permanentlyDeleteMarkedNotifications()
    }

    suspend fun getNotificationsPagedSync(limit: Int, offset: Int): List<NotificationEntity> {
        return notificationDao.getNotificationsPagedSync(limit, offset)
    }

    fun getNotificationsPaged(limit: Int, offset: Int): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsPaged(limit, offset)
    }

    // Helper function to create notifications
    suspend fun createBillReminderNotification(
        billId: Long,
        billName: String,
        dueDate: String,
        amount: String
    ) {
        val notification = NotificationEntity(
            type = "bill_reminder",
            title = "Bill Reminder",
            message = "$billName due on $dueDate - $amount",
            relatedId = billId,
            relatedType = "BillReminder"
        )
        insertNotification(notification)
    }

    suspend fun createBudgetAlertNotification(
        budgetId: Long,
        category: String,
        percentageUsed: Double
    ) {
        val notification = NotificationEntity(
            type = "budget_alert",
            title = "Budget Alert",
            message = "You've spent ${
                String.format(
                    "%.0f",
                    percentageUsed
                )
            }% of your $category budget",
            relatedId = budgetId,
            relatedType = "Budget"
        )
        insertNotification(notification)
    }

    suspend fun createSavingsGoalNotification(
        goalId: Long,
        goalName: String,
        percentageComplete: Double
    ) {
        val notification = NotificationEntity(
            type = "savings_goal",
            title = "Savings Goal Update",
            message = "You've reached ${
                String.format(
                    "%.0f",
                    percentageComplete
                )
            }% of your $goalName goal",
            relatedId = goalId,
            relatedType = "SavingsGoal"
        )
        insertNotification(notification)
    }

    suspend fun createDebtReminderNotification(
        debtId: Long,
        personName: String,
        dueDate: String,
        amount: String
    ) {
        val notification = NotificationEntity(
            type = "debt_reminder",
            title = "Debt Reminder",
            message = "Debt to $personName due on $dueDate - $amount",
            relatedId = debtId,
            relatedType = "Debt"
        )
        insertNotification(notification)
    }

    suspend fun createGeneralNotification(title: String, message: String) {
        val notification = NotificationEntity(
            type = "general",
            title = title,
            message = message
        )
        insertNotification(notification)
    }
}
