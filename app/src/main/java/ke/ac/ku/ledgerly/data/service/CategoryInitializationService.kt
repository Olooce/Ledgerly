package ke.ac.ku.ledgerly.data.service

import ke.ac.ku.ledgerly.data.dao.CategoryDao
import ke.ac.ku.ledgerly.data.model.CategoryEntity
import javax.inject.Inject

class CategoryInitializationService @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun initializeDefaultCategories() {
        // Check if categories are already initialized
        val existingCount = categoryDao.getCategoriesCount()
        if (existingCount > 0) {
            return // Categories already exist
        }

        // Create default expense categories
        val defaultExpenseCategories = listOf(
            CategoryEntity(
                id = "grocery",
                name = "Grocery",
                icon = 0,
                color = -11935381L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "netflix",
                name = "Netflix",
                icon = 0,
                color = -52480L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "rent",
                name = "Rent",
                icon = 0,
                color = -4207945L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "paypal",
                name = "Paypal",
                icon = 0,
                color = -16776961L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "starbucks",
                name = "Starbucks",
                icon = 0,
                color = -8410369L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "shopping",
                name = "Shopping",
                icon = 0,
                color = -12189568L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "transport",
                name = "Transport",
                icon = 0,
                color = -6710887L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "utilities",
                name = "Utilities",
                icon = 0,
                color = -4147200L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "dining_out",
                name = "Dining Out",
                icon = 0,
                color = -13395456L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "entertainment",
                name = "Entertainment",
                icon = 0,
                color = -61681L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "healthcare",
                name = "Healthcare",
                icon = 0,
                color = -8847360L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "insurance",
                name = "Insurance",
                icon = 0,
                color = -1744830L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "subscriptions",
                name = "Subscriptions",
                icon = 0,
                color = -3670016L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "education",
                name = "Education",
                icon = 0,
                color = -5317953L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "debt_payments",
                name = "Debt Payments",
                icon = 0,
                color = -2236962L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "gifts_donations",
                name = "Gifts & Donations",
                icon = 0,
                color = -1275068L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "travel",
                name = "Travel",
                icon = 0,
                color = -12087627L,
                isDefault = true,
                categoryType = "Expense"
            ),
            CategoryEntity(
                id = "other_expenses",
                name = "Other Expenses",
                icon = 0,
                color = -3355444L,
                isDefault = true,
                categoryType = "Expense"
            )
        )

        // Create default income categories
        val defaultIncomeCategories = listOf(
            CategoryEntity(
                id = "salary",
                name = "Salary",
                icon = 0,
                color = -3713642L,
                isDefault = true,
                categoryType = "Income"
            ),
            CategoryEntity(
                id = "freelance",
                name = "Freelance",
                icon = 0,
                color = -14575885L,
                isDefault = true,
                categoryType = "Income"
            ),
            CategoryEntity(
                id = "investments",
                name = "Investments",
                icon = 0,
                color = -8454016L,
                isDefault = true,
                categoryType = "Income"
            ),
            CategoryEntity(
                id = "bonus",
                name = "Bonus",
                icon = 0,
                color = -4725256L,
                isDefault = true,
                categoryType = "Income"
            ),
            CategoryEntity(
                id = "rental_income",
                name = "Rental Income",
                icon = 0,
                color = -10702155L,
                isDefault = true,
                categoryType = "Income"
            ),
            CategoryEntity(
                id = "other_income",
                name = "Other Income",
                icon = 0,
                color = -3355444L,
                isDefault = true,
                categoryType = "Income"
            )
        )

        val allCategories = defaultExpenseCategories + defaultIncomeCategories
        categoryDao.insertCategories(allCategories)
    }
}
