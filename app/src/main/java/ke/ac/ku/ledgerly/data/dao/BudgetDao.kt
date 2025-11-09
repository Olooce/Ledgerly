package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.BudgetEntity

@Dao
interface BudgetDao {
    suspend fun insertBudgetWithTimestamp(transactionDao: TransactionDao, budget: BudgetEntity) {
        insertBudget(budget.copy(lastModified = System.currentTimeMillis()))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear ORDER BY lastModified DESC")
    suspend fun getBudgetsForMonth(monthYear: String): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE category = :category AND monthYear = :monthYear ORDER BY lastModified DESC")
    suspend fun getBudgetForCategory(category: String, monthYear: String): BudgetEntity?

    @Query("DELETE FROM budgets WHERE category = :category AND monthYear = :monthYear")
    suspend fun deleteBudget(category: String, monthYear: String)

    // Get current spending for a category in a specific month
    @Query(
        """
    SELECT COALESCE(SUM(amount), 0)
    FROM transactions
    WHERE category = :category
      AND type = 'Expense'
      AND strftime('%Y-%m', datetime(date / 1000, 'unixepoch')) = :monthYear
"""
    )
    suspend fun getCurrentSpendingForCategory(category: String, monthYear: String): Double


    @Query("SELECT * FROM budgets ORDER BY lastModified DESC")
    suspend fun getAllBudgetsSync(): List<BudgetEntity>

}

