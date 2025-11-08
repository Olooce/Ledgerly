package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Delete
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

    @Delete
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransactionEntity)

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1")
    suspend fun getActiveRecurringTransactions(): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getRecurringTransactionById(id: Long): RecurringTransactionEntity?

    @Query("UPDATE recurring_transactions SET isActive = :isActive WHERE id = :id")
    suspend fun updateRecurringTransactionStatus(id: Long, isActive: Boolean)

    @Query("SELECT * FROM recurring_transactions")
    suspend fun getAllRecurringTransactionsSync(): List<RecurringTransactionEntity>

}