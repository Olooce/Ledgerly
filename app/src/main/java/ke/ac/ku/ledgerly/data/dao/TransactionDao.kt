package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {


    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'Expense' ORDER BY amount DESC LIMIT 5")
    fun getTopExpenses(): Flow<List<TransactionEntity>>


    @Query("SELECT type, date, SUM(amount) AS total_amount FROM transactions where type = :type GROUP BY type, date ORDER BY date")
    fun getAllExpenseByDate(type: String = "Expense"): Flow<List<TransactionSummary>>

    @Insert
    suspend fun insertExpense(transactionEntity: TransactionEntity)

    @Delete
    suspend fun deleteExpense(transactionEntity: TransactionEntity)

    @Update
    suspend fun updateExpense(transactionEntity: TransactionEntity)
}