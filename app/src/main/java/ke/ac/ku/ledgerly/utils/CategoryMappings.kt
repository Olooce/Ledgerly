package ke.ac.ku.ledgerly.utils

object CategoryMappings {
    // Map old category names to new category IDs
    private val oldNameToCategoryId = mapOf(
        "Grocery" to "grocery",
        "Netflix" to "netflix",
        "Rent" to "rent",
        "Paypal" to "paypal",
        "Starbucks" to "starbucks",
        "Shopping" to "shopping",
        "Transport" to "transport",
        "Utilities" to "utilities",
        "Dining Out" to "dining_out",
        "Entertainment" to "entertainment",
        "Healthcare" to "healthcare",
        "Insurance" to "insurance",
        "Subscriptions" to "subscriptions",
        "Education" to "education",
        "Debt Payments" to "debt_payments",
        "Gifts & Donations" to "gifts_donations",
        "Travel" to "travel",
        "Other Expenses" to "other_expenses",
        "Salary" to "salary",
        "Freelance" to "freelance",
        "Investments" to "investments",
        "Bonus" to "bonus",
        "Rental Income" to "rental_income",
        "Other Income" to "other_income"
    )

    // Map category names to icons using Utils function
    fun getIconForCategoryName(categoryName: String): Int {
        return Utils.getItemIcon(categoryName)
    }

    // Map category IDs to default colors
    private val categoryIdToColor = mapOf(
        "grocery" to -11935381L,
        "netflix" to -52480L,
        "rent" to -4207945L,
        "paypal" to -16776961L,
        "starbucks" to -8410369L,
        "shopping" to -12189568L,
        "transport" to -6710887L,
        "utilities" to -4147200L,
        "dining_out" to -13395456L,
        "entertainment" to -61681L,
        "healthcare" to -8847360L,
        "insurance" to -1744830L,
        "subscriptions" to -3670016L,
        "education" to -5317953L,
        "debt_payments" to -2236962L,
        "gifts_donations" to -1275068L,
        "travel" to -12087627L,
        "other_expenses" to -3355444L,
        "salary" to -3713642L,
        "freelance" to -14575885L,
        "investments" to -8454016L,
        "bonus" to -4725256L,
        "rental_income" to -10702155L,
        "other_income" to -3355444L
    )

    fun mapOldCategoryNameToId(categoryName: String): String {
        return oldNameToCategoryId[categoryName] ?: categoryName.lowercase().replace(" ", "_")
    }

    fun getColorForCategoryId(categoryId: String): Long {
        return categoryIdToColor[categoryId] ?: -6710887L
    }
}
