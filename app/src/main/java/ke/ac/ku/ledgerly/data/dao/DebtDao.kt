package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.DebtEntity
import ke.ac.ku.ledgerly.data.model.DebtSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts WHERE isDeleted = 0 ORDER BY dueDate ASC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE isDeleted = 0 ORDER BY dueDate ASC")
    suspend fun getAllDebtSync(): List<DebtEntity>

    @Query("SELECT * FROM debts WHERE id = :id AND isDeleted = 0")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Query("SELECT * FROM debts WHERE id = :id AND isDeleted = 0")
    fun getDebtByIdFlow(id: Long?): Flow<DebtEntity?>

    @Query(
        """
        SELECT * FROM debts 
        WHERE isDeleted = 0 
        AND debtType = :debtType 
        ORDER BY dueDate ASC
    """
    )
    fun getDebtsByType(debtType: String): Flow<List<DebtEntity>>

    @Query(
        """
        SELECT * FROM debts 
        WHERE isDeleted = 0 
        AND status = :status 
        ORDER BY dueDate ASC
    """
    )
    fun getDebtsByStatus(status: String): Flow<List<DebtEntity>>

    @Query(
        """
        SELECT * FROM debts 
        WHERE isDeleted = 0 
        AND dueDate < :currentTime 
        AND status != 'settled'
        ORDER BY dueDate ASC
    """
    )
    fun getOverdueDebts(currentTime: Long = System.currentTimeMillis()): Flow<List<DebtEntity>>

    @Query(
        """
        SELECT * FROM debts 
        WHERE isDeleted = 0 
        AND dueDate BETWEEN :startTime AND :endTime
        ORDER BY dueDate ASC
    """
    )
    fun getUpcomingDebts(startTime: Long, endTime: Long): Flow<List<DebtEntity>>

    @Query(
        """
        SELECT * FROM debts 
        WHERE isDeleted = 0 
        AND reminderEnabled = 1 
        AND dueDate <= :reminderTime 
        AND status != 'settled'
    """
    )
    suspend fun getDebtsNeedingReminder(reminderTime: Long = System.currentTimeMillis()): List<DebtEntity>

    @Query(
        """
        SELECT SUM(CASE WHEN debtType = 'owe' THEN amount ELSE 0 END) as totalOwe,
               SUM(CASE WHEN debtType = 'owed' THEN amount ELSE 0 END) as totalOwed,
               COUNT(CASE WHEN dueDate < :currentTime AND status != 'settled' THEN 1 END) as overdueCount,
               COUNT(CASE WHEN dueDate >= :currentTime AND status = 'pending' THEN 1 END) as upcomingCount
        FROM debts 
        WHERE isDeleted = 0
    """
    )
    fun getDebtsSummary(currentTime: Long = System.currentTimeMillis()): Flow<DebtSummary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Query("UPDATE debts SET isDeleted = 1, lastModified = :timestamp WHERE id = :id")
    suspend fun softDeleteDebt(id: Long, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("UPDATE debts SET status = :status, lastModified = :timestamp WHERE id = :id")
    suspend fun updateDebtStatus(
        id: Long,
        status: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM debts WHERE isDeleted = 1")
    suspend fun permanentlyDeleteOldDebts()
}
