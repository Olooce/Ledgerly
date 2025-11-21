package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.CategorySummary
import ke.ac.ku.ledgerly.data.model.MonthlyComparison
import ke.ac.ku.ledgerly.data.model.MonthlyTotals
import ke.ac.ku.ledgerly.data.model.MonthlyTrend
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsIncludingDeleted(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE type = 'Expense' AND isDeleted = 0 ORDER BY amount DESC LIMIT 5")
    fun getTopExpenses(): Flow<List<TransactionEntity>>

    @Query("SELECT type, date, SUM(amount) AS total_amount FROM transactions WHERE type = :type AND isDeleted = 0 GROUP BY type, date ORDER BY date")
    fun getAllExpenseByDate(type: String = "Expense"): Flow<List<TransactionSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    suspend fun insertTransactionWithTimestamp(transaction: TransactionEntity) {
        insertTransaction(transaction.copy(lastModified = System.currentTimeMillis()))
    }

    @Query("UPDATE transactions SET isDeleted = 1, lastModified = :timestamp WHERE id = :id")
    suspend fun softDeleteTransaction(id: Long?, timestamp: Long = System.currentTimeMillis())

    @Update
    suspend fun updateTransaction(transactionEntity: TransactionEntity)

    @Query(
        """
SELECT category, SUM(amount) as total_amount 
FROM transactions 
WHERE type = 'Expense' 
AND isDeleted = 0
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
WHERE isDeleted = 0
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
AND isDeleted = 0
AND date IS NOT NULL 
GROUP BY strftime('%Y-%m', datetime(date / 1000, 'unixepoch')), category
HAVING total_amount > 0
ORDER BY month
"""
    )
    fun getMonthlySpendingTrends(): Flow<List<MonthlyTrend>>


    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPaginated(limit: Int, offset: Int): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE isDeleted = 0")
    suspend fun getTransactionsCount(): Int

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getTransactionsPaginatedFlow(limit: Int, offset: Int): Flow<List<TransactionEntity>>

    @Query(
        """
    SELECT * FROM transactions 
    WHERE isDeleted = 0 
    AND (:filterType = 'All' OR type = :filterType)
    AND (:searchQuery = '' OR category LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%')
    AND (:minAmount = -1 OR amount >= :minAmount)
    AND (:maxAmount = -1 OR amount <= :maxAmount)
    AND (:categoriesCount = 0 OR category IN (:categories))
    AND (:dateRange = 'All Time' OR date BETWEEN :startDate AND :endDate)
    ORDER BY date DESC 
    LIMIT :limit OFFSET :offset
"""
    )
    suspend fun getFilteredTransactionsPaginated(
        filterType: String,
        searchQuery: String,
        dateRange: String,
        startDate: Long,
        endDate: Long,
        minAmount: Double,
        maxAmount: Double,
        categoriesCount: Int,
        categories: List<String>,
        limit: Int,
        offset: Int
    ): List<TransactionEntity>

    @Query(
        """
    SELECT COUNT(*) FROM transactions 
    WHERE isDeleted = 0 
    AND (:filterType = 'All' OR type = :filterType)
    AND (:searchQuery = '' OR category LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%')
    AND (:minAmount = -1 OR amount >= :minAmount)
    AND (:maxAmount = -1 OR amount <= :maxAmount)
    AND (:categoriesCount = 0 OR category IN (:categories))
    AND (:dateRange = 'All Time' OR date BETWEEN :startDate AND :endDate)
"""
    )
    suspend fun getFilteredTransactionsCount(
        filterType: String,
        searchQuery: String,
        dateRange: String,
        startDate: Long,
        endDate: Long,
        minAmount: Double,
        maxAmount: Double,
        categoriesCount: Int,
        categories: List<String>
    ): Int

    @Query(
        """
SELECT * FROM transactions 
WHERE isDeleted = 0
AND strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) = :monthYear
ORDER BY date DESC
LIMIT :limit OFFSET :offset
"""
    )
    suspend fun getTransactionsForMonthPaginated(
        monthYear: String,
        limit: Int,
        offset: Int
    ): List<TransactionEntity>

    @Query(
        """
    SELECT 
        SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) AS totalIncome,
        SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS totalExpense
    FROM transactions
    WHERE isDeleted = 0
      AND strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) = :monthYear
    """
    )
    suspend fun getMonthlyTotals(monthYear: String): MonthlyTotals?


}