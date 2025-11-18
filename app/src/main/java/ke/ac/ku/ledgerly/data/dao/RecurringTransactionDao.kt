package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransactionEntity): Long

    suspend fun insertRecurringTransactionWithTimestamp(recurringTransaction: RecurringTransactionEntity) {
        insertRecurringTransaction(recurringTransaction.copy(lastModified = System.currentTimeMillis()))
    }

    @Update
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransactionEntity)

    @Query("UPDATE recurring_transactions SET isDeleted = 1, lastModified = :timestamp WHERE id = :id")
    suspend fun softDeleteRecurringTransaction(id: Long?, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND isDeleted = 0 ORDER BY startDate DESC")
    suspend fun getActiveRecurringTransactions(): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions WHERE isDeleted = 0 ORDER BY startDate DESC")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id AND isDeleted = 0")
    suspend fun getRecurringTransactionById(id: Long): RecurringTransactionEntity?

    @Query("UPDATE recurring_transactions SET isActive = :isActive WHERE id = :id")
    suspend fun updateRecurringTransactionStatus(id: Long, isActive: Boolean)

    @Query("SELECT * FROM recurring_transactions WHERE isDeleted = 0 ORDER BY startDate DESC")
    suspend fun getAllRecurringTransactionsSync(): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions ORDER BY startDate DESC")
    suspend fun getAllRecurringTransactionsIncludingDeleted(): List<RecurringTransactionEntity>
}