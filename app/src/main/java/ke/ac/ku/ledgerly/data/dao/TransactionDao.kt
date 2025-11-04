package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.BudgetEntity
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
}