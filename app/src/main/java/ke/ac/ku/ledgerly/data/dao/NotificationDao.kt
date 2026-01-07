package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.NotificationEntity
import ke.ac.ku.ledgerly.data.model.NotificationSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)

    @Query("SELECT * FROM notifications WHERE id = :id AND isDeleted = 0")
    suspend fun getNotificationById(id: Long): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE isDeleted = 0 ORDER BY createdDate DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isRead = 0 AND isDeleted = 0 ORDER BY createdDate DESC")
    fun getUnreadNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isRead = 0 AND isDeleted = 0 ORDER BY createdDate DESC LIMIT :limit")
    fun getUnreadNotificationsLimited(limit: Int = 5): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0 AND isDeleted = 0")
    fun getUnreadNotificationCount(): Flow<Int>

    @Query(
        """
        SELECT 
            CAST(COUNT(CASE WHEN isRead = 0 AND isDeleted = 0 THEN 1 END) AS INTEGER) as unreadCount,
            CAST(COUNT(CASE WHEN isDeleted = 0 THEN 1 END) AS INTEGER) as totalCount
        FROM notifications
    """
    )
    fun getNotificationSummary(): Flow<NotificationSummary>

    @Query("SELECT * FROM notifications WHERE type = :type AND isDeleted = 0 ORDER BY createdDate DESC")
    fun getNotificationsByType(type: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE relatedId = :relatedId AND relatedType = :relatedType AND isDeleted = 0")
    suspend fun getNotificationsByRelatedEntity(
        relatedId: Long,
        relatedType: String
    ): List<NotificationEntity>

    @Query("UPDATE notifications SET isRead = 1, readDate = :readDate, lastModified = :lastModified WHERE id = :id")
    suspend fun markAsRead(
        id: Long,
        readDate: Long = System.currentTimeMillis(),
        lastModified: Long = System.currentTimeMillis()
    )

    @Query("UPDATE notifications SET isRead = 1, readDate = :readDate, lastModified = :lastModified WHERE isDeleted = 0")
    suspend fun markAllAsRead(
        readDate: Long = System.currentTimeMillis(),
        lastModified: Long = System.currentTimeMillis()
    )

    @Query("UPDATE notifications SET isDeleted = 1, lastModified = :lastModified WHERE id = :id")
    suspend fun softDeleteNotification(id: Long, lastModified: Long = System.currentTimeMillis())

    @Query("UPDATE notifications SET isDeleted = 1, lastModified = :lastModified WHERE type = :type AND isDeleted = 0")
    suspend fun softDeleteNotificationsByType(
        type: String,
        lastModified: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM notifications WHERE isDeleted = 1")
    suspend fun permanentlyDeleteMarkedNotifications()

    @Query("SELECT * FROM notifications WHERE isDeleted = 0 ORDER BY createdDate DESC LIMIT :limit OFFSET :offset")
    suspend fun getNotificationsPagedSync(limit: Int, offset: Int): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE isDeleted = 0 ORDER BY createdDate DESC LIMIT :limit OFFSET :offset")
    fun getNotificationsPaged(limit: Int, offset: Int): Flow<List<NotificationEntity>>
}
