package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.data.model.CategorySummary
import ke.ac.ku.ledgerly.data.model.MonthlyComparison
import ke.ac.ku.ledgerly.data.model.MonthlyTrend
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE type = 'Expense' ORDER BY amount DESC LIMIT 5")
    fun getTopExpenses(): Flow<List<TransactionEntity>>

    @Query("SELECT type, date, SUM(amount) AS total_amount FROM transactions where type = :type GROUP BY type, date ORDER BY date")
    fun getAllExpenseByDate(type: String = "Expense"): Flow<List<TransactionSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transactionEntity: TransactionEntity)

    @Update
    suspend fun updateTransaction(transactionEntity: TransactionEntity)

    // Budget methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear")
    suspend fun getBudgetsForMonth(monthYear: String): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE category = :category AND monthYear = :monthYear")
    suspend fun getBudgetForCategory(category: String, monthYear: String): BudgetEntity?

    @Query("DELETE FROM budgets WHERE category = :category AND monthYear = :monthYear")
    suspend fun deleteBudget(category: String, monthYear: String)

    // Get current spending for a category in a specific month
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE category = :category AND type = 'Expense' AND strftime('%Y-%m', date) = :monthYear")
    suspend fun getCurrentSpendingForCategory(category: String, monthYear: String): Double

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgetsSync(): List<BudgetEntity>


    // Recurring transaction methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransactionEntity): Long

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

    @Query("""
    SELECT category, SUM(amount) as total_amount 
    FROM transactions 
    WHERE type = 'Expense' 
    AND strftime('%Y-%m', date) = :monthYear 
    GROUP BY category
    HAVING total_amount > 0
""")
    fun getExpenseByCategoryForMonth(monthYear: String): Flow<List<CategorySummary>>

    @Query("""
    SELECT strftime('%Y-%m', date) as month, 
           SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) as income,
           SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) as expense
    FROM transactions 
    WHERE date IS NOT NULL AND strftime('%Y-%m', date) IS NOT NULL
    GROUP BY strftime('%Y-%m', date)
    HAVING income > 0 OR expense > 0
    ORDER BY month
""")
    fun getMonthlyIncomeVsExpense(): Flow<List<MonthlyComparison>>

    @Query("""
    SELECT strftime('%Y-%m', date) as month,
           category,
           SUM(amount) as total_amount
    FROM transactions 
    WHERE type = 'Expense' 
    AND date IS NOT NULL 
    AND strftime('%Y-%m', date) IS NOT NULL
    GROUP BY strftime('%Y-%m', date), category
    HAVING total_amount > 0
    ORDER BY month
""")
    fun getMonthlySpendingTrends(): Flow<List<MonthlyTrend>>
}
