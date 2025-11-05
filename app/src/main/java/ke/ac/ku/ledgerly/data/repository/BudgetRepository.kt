package ke.ac.ku.ledgerly.data.repository

import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.utils.Utils
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    suspend fun setBudget(budget: BudgetEntity) {
        transactionDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) {
        transactionDao.updateBudget(budget)
    }

    suspend fun getBudgetsForCurrentMonth(): List<BudgetEntity> {
        val currentMonth = Utils.getCurrentMonthYear()
        return transactionDao.getBudgetsForMonth(currentMonth)
    }

    suspend fun getBudgetForCategory(category: String): BudgetEntity? {
        val currentMonth = Utils.getCurrentMonthYear()
        return transactionDao.getBudgetForCategory(category, currentMonth)
    }

    suspend fun deleteBudget(category: String) {
        val currentMonth = Utils.getCurrentMonthYear()
        transactionDao.deleteBudget(category, currentMonth)
    }

    suspend fun refreshBudgetSpending() {
        val currentMonth = Utils.getCurrentMonthYear()
        val budgets = transactionDao.getBudgetsForMonth(currentMonth)

        budgets.forEach { budget ->
            val currentSpending =
                transactionDao.getCurrentSpendingForCategory(budget.category, currentMonth)
            if (budget.currentSpending != currentSpending) {
                val updatedBudget = budget.copy(currentSpending = currentSpending)
                transactionDao.updateBudget(updatedBudget)
            }
        }
    }

    suspend fun getBudgetsExceedingThreshold(threshold: Int = 80): List<BudgetEntity> {
        val budgets = getBudgetsForCurrentMonth()
        return budgets.filter { it.isNearLimit(threshold) }
    }
}