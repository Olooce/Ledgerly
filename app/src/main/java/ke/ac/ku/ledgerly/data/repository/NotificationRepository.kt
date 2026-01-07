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
    }
