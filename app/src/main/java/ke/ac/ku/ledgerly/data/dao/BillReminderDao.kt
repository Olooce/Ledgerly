package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.BillReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillReminder(billReminder: BillReminderEntity): Long

    @Update
    suspend fun updateBillReminder(billReminder: BillReminderEntity)

    @Delete
    suspend fun deleteBillReminder(billReminder: BillReminderEntity)

    @Query("DELETE FROM bill_reminders WHERE id = :id")
    suspend fun deleteBillReminderById(id: Long)

    @Query("SELECT * FROM bill_reminders WHERE id = :id AND isDeleted = 0")
    suspend fun getBillReminderById(id: Long): BillReminderEntity?

    @Query("SELECT * FROM bill_reminders WHERE isDeleted = 0 ORDER BY dueDate ASC")
    fun getAllBillReminders(): Flow<List<BillReminderEntity>>

    @Query("SELECT * FROM bill_reminders WHERE isDeleted = 0 ORDER BY dueDate ASC")
    suspend fun getAllBillRemindersOnce(): List<BillReminderEntity>

    @Query(
        """
        SELECT * FROM bill_reminders 
        WHERE isDeleted = 0 AND status = 'pending' 
        AND dueDate > :startDate AND dueDate <= :endDate
        ORDER BY dueDate ASC
    """
    )
    fun getUpcomingBills(startDate: Long, endDate: Long): Flow<List<BillReminderEntity>>

    @Query(
        """
        SELECT * FROM bill_reminders 
        WHERE isDeleted = 0 AND status = 'pending' 
        AND dueDate < :currentTime
        ORDER BY dueDate DESC
    """
    )
    fun getOverdueBills(currentTime: Long): Flow<List<BillReminderEntity>>

    @Query(
        """
        SELECT * FROM bill_reminders 
        WHERE isDeleted = 0 AND status = 'pending'
        AND reminderEnabled = 1
        AND notificationSent = 0
        AND dueDate <= :reminderTime
        ORDER BY dueDate ASC
    """
    )
    suspend fun getBillsNeedingReminder(reminderTime: Long): List<BillReminderEntity>

    @Query(
        """
        SELECT * FROM bill_reminders 
        WHERE isDeleted = 0 
        AND category = :category
        ORDER BY dueDate ASC
    """
    )
    fun getBillsByCategory(category: String): Flow<List<BillReminderEntity>>

    @Query(
        """
        SELECT * FROM bill_reminders 
        WHERE isDeleted = 0 AND status = 'pending'
        ORDER BY dueDate ASC
        LIMIT :limit
    """
    )
    fun getUpcomingBillsLimited(limit: Int = 5): Flow<List<BillReminderEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM bill_reminders 
        WHERE isDeleted = 0 AND status = 'pending' 
        AND dueDate < :currentTime
    """
    )
    suspend fun getOverdueBillsCount(currentTime: Long): Int

    @Query(
        """
        SELECT SUM(amount) FROM bill_reminders 
        WHERE isDeleted = 0 AND status = 'pending'
        AND dueDate > :startDate AND dueDate <= :endDate
    """
    )
    suspend fun getTotalUpcomingAmount(startDate: Long, endDate: Long): Double?

    @Query(
        """
        UPDATE bill_reminders 
        SET notificationSent = 1, lastModified = :timestamp
        WHERE id = :id
    """
    )
    suspend fun markReminderSent(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query(
        """
        UPDATE bill_reminders 
        SET status = :status, isOverdue = :isOverdue, lastModified = :timestamp
        WHERE id = :id
    """
    )
    suspend fun updateBillStatus(
        id: Long,
        status: String,
        isOverdue: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE bill_reminders 
        SET isDeleted = 1, lastModified = :timestamp
        WHERE id = :id
    """
    )
    suspend fun softDeleteBillReminder(id: Long, timestamp: Long = System.currentTimeMillis())


    @Query(
        """
    SELECT COUNT(*) FROM bill_reminders
    WHERE status = 'pending'
      AND isDeleted = 0
      AND dueDate >= :startOfMonth
      AND dueDate < :startOfNextMonth
"""
    )
    suspend fun getThisMonthBillsCount(
        startOfMonth: Long,
        startOfNextMonth: Long
    ): Int


}
