package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.CategorySummary
import ke.ac.ku.ledgerly.data.model.MonthlyComparison
import ke.ac.ku.ledgerly.data.model.MonthlyTrend
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE type = 'Expense' ORDER BY amount DESC LIMIT 5")
    fun getTopExpenses(): Flow<List<TransactionEntity>>

    @Query("SELECT type, date, SUM(amount) AS total_amount FROM transactions where type = :type GROUP BY type, date ORDER BY date")
    fun getAllExpenseByDate(type: String = "Expense"): Flow<List<TransactionSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    suspend fun insertTransactionWithTimestamp(transaction: TransactionEntity) {
        insertTransaction(transaction.copy(lastModified = System.currentTimeMillis()))
    }

    @Delete
    suspend fun deleteTransaction(transactionEntity: TransactionEntity)

    @Update
    suspend fun updateTransaction(transactionEntity: TransactionEntity)


    @Query(
        """
SELECT category, SUM(amount) as total_amount 
FROM transactions 
WHERE type = 'Expense' 
AND strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) = :monthYear 
GROUP BY category
HAVING total_amount > 0
"""
    )
    fun getExpenseByCategoryForMonth(monthYear: String): Flow<List<CategorySummary>>

    @Query(
        """
SELECT strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) AS month,
       SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) AS income,
       SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS expense
FROM transactions
GROUP BY month
ORDER BY month
"""
    )
    fun getMonthlyIncomeVsExpense(): Flow<List<MonthlyComparison>>


    @Query(
        """
SELECT strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) as month,
       category,
       SUM(amount) as total_amount
FROM transactions 
WHERE type = 'Expense' 
AND date IS NOT NULL 
GROUP BY strftime('%Y-%m', datetime(date / 1000, 'unixepoch')), category
HAVING total_amount > 0
ORDER BY month
"""
    )
    fun getMonthlySpendingTrends(): Flow<List<MonthlyTrend>>
}
