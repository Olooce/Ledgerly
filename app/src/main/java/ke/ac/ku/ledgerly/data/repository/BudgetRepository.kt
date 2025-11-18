package ke.ac.ku.ledgerly.data.repository

import ke.ac.ku.ledgerly.data.dao.BudgetDao
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.utils.Utils
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    private val dao: BudgetDao
) {
    suspend fun setBudget(budget: BudgetEntity) {
        dao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) {
        dao.updateBudget(budget)
    }

    suspend fun getBudgetsForCurrentMonth(): List<BudgetEntity> {
        val currentMonth = Utils.getCurrentMonthYear()
        return dao.getBudgetsForMonth(currentMonth)
    }

    suspend fun getBudgetForCategory(category: String): BudgetEntity? {
        val currentMonth = Utils.getCurrentMonthYear()
        return dao.getBudgetForCategory(category, currentMonth)
    }

    suspend fun deleteBudget(category: String) {
        val currentMonth = Utils.getCurrentMonthYear()
        dao.softDeleteBudget(category, currentMonth)
    }

    suspend fun refreshBudgetSpending() {
        val currentMonth = Utils.getCurrentMonthYear()
        val budgets = dao.getBudgetsForMonth(currentMonth)

        budgets.forEach { budget ->
            val currentSpending = dao.getCurrentSpendingForCategory(budget.category, currentMonth)
            if (budget.currentSpending != currentSpending) {
                val updatedBudget = budget.copy(currentSpending = currentSpending)
                dao.updateBudget(updatedBudget)
            }
        }
    }

    suspend fun getBudgetsExceedingThreshold(threshold: Int = 80): List<BudgetEntity> {
        val budgets = getBudgetsForCurrentMonth()
        return budgets.filter { it.isNearLimit(threshold) }
    }
}