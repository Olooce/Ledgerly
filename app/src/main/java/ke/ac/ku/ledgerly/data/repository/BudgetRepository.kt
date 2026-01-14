package ke.ac.ku.ledgerly.data.repository

import ke.ac.ku.ledgerly.data.dao.BudgetDao
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.domain.CurrencyManager
import ke.ac.ku.ledgerly.utils.Utils
import java.math.BigDecimal
import javax.inject.Inject


class BudgetRepository @Inject constructor(
    private val dao: BudgetDao,
    private val currencyManager: CurrencyManager
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
            val currentSpendingUsd =
                dao.getCurrentSpendingForCategoryUsd(budget.category, currentMonth)
            if (budget.currentSpending != currentSpendingUsd) {
                val updatedBudget = budget.copy(currentSpending = currentSpendingUsd)
                dao.updateBudget(updatedBudget)
            }
        }
    }

    suspend fun getBudgetsExceedingThreshold(threshold: Int = 80): List<BudgetEntity> {
        val budgets = getBudgetsForCurrentMonth()
        return budgets.filter { it.isNearLimit(threshold) }
    }


    suspend fun getBudgetForDisplay(category: String): BudgetEntity? {
        val budget = getBudgetForCategory(category) ?: return null
        return convertBudgetForDisplay(budget)
    }

    suspend fun getBudgetsForDisplayCurrentMonth(): List<BudgetEntity> {
        val budgets = getBudgetsForCurrentMonth()
        return budgets.map { convertBudgetForDisplay(it) }
    }


    private suspend fun convertBudgetForDisplay(budget: BudgetEntity): BudgetEntity {
        val displayCurrency = currencyManager.getDisplayCurrency()
        if (displayCurrency == "USD") {
            return budget
        }

        val displayBudgetUsd: BigDecimal =
            currencyManager.convertToDisplayCurrency(budget.monthlyBudget, displayCurrency)
                .toBigDecimal()
        val displaySpendingUsd: BigDecimal =
            currencyManager.convertToDisplayCurrency(budget.currentSpending, displayCurrency)
                .toBigDecimal()


        return budget.copy(
            monthlyBudget = displayBudgetUsd,
            currentSpending = displaySpendingUsd
        )
    }

    suspend fun getTotalSpendingUsdForMonth(monthYear: String): BigDecimal {
        return dao.getTotalSpendingUsdForMonth(monthYear)
    }

    suspend fun getTotalBudgetUsdForMonth(monthYear: String): BigDecimal {
        val budgets = dao.getBudgetsForMonth(monthYear)
        return budgets.fold(BigDecimal.ZERO) { acc, budget ->
            acc + budget.monthlyBudget
        }
    }
}