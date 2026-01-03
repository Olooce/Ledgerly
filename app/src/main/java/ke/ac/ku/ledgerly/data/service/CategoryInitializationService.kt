package ke.ac.ku.ledgerly.data.service

import ke.ac.ku.ledgerly.data.dao.CategoryDao
import ke.ac.ku.ledgerly.data.model.CategoryEntity
import ke.ac.ku.ledgerly.utils.Utils
import javax.inject.Inject

class CategoryInitializationService @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun initializeDefaultCategories() {
        // Check if categories are already initialized
        val existingCount = categoryDao.getCategoriesCount()
        if (existingCount > 0) return // Categories already exist

        val expenseData = listOf(
            Quad("Grocery", "Grocery", -11935381L, "Expense"),
            Quad("Netflix", "Netflix", -52480L, "Expense"),
            Quad("Rent", "Rent", -4207945L, "Expense"),
            Quad("Paypal", "Paypal", -16776961L, "Expense"),
            Quad("Starbucks", "Starbucks", -8410369L, "Expense"),
            Quad("Shopping", "Shopping", -12189568L, "Expense"),
            Quad("Transport", "Transport", -6710887L, "Expense"),
            Quad("Utilities", "Utilities", -4147200L, "Expense"),
            Quad("Dining_out", "Dining Out", -13395456L, "Expense"),
            Quad("Entertainment", "Entertainment", -61681L, "Expense"),
            Quad("Healthcare", "Healthcare", -8847360L, "Expense"),
            Quad("Insurance", "Insurance", -1744830L, "Expense"),
            Quad("Subscriptions", "Subscriptions", -3670016L, "Expense"),
            Quad("Education", "Education", -5317953L, "Expense"),
            Quad("Debt_payments", "Debt Payments", -2236962L, "Expense"),
            Quad("Gifts_donations", "Gifts & Donations", -1275068L, "Expense"),
            Quad("Travel", "Travel", -12087627L, "Expense"),
            Quad("other_expenses", "Other Expenses", -3355444L, "Expense")
        )

        val incomeData = listOf(
            Quad("Salary", "Salary", -3713642L, "Income"),
            Quad("Freelance", "Freelance", -14575885L, "Income"),
            Quad("Investments", "Investments", -8454016L, "Income"),
            Quad("Bonus", "Bonus", -4725256L, "Income"),
            Quad("Rental_income", "Rental Income", -10702155L, "Income"),
            Quad("Other_income", "Other Income", -3355444L, "Income")
        )

        val defaultExpenseCategories = expenseData.map { (id, name, color, type) ->
            CategoryEntity(
                id = id,
                name = name,
                icon = Utils.getItemIcon(id),
                color = color,
                isDefault = true,
                categoryType = type
            )
        }

        val defaultIncomeCategories = incomeData.map { (id, name, color, type) ->
            CategoryEntity(
                id = id,
                name = name,
                icon = Utils.getItemIcon(id),
                color = color,
                isDefault = true,
                categoryType = type
            )
        }

        val allCategories = defaultExpenseCategories + defaultIncomeCategories
        categoryDao.insertCategories(allCategories)
    }

    private data class Quad(val id: String, val name: String, val color: Long, val type: String)
}
