package ke.ac.ku.ledgerly.data.model

data class TransactionSummary(
    val type: String,
    val date: String,
    val total_amount: Double
)

data class CategorySummary(
    val category: String,
    val total_amount: Double
)

data class MonthlyComparison(
    val month: String?,
    val income: Double,
    val expense: Double
)

data class MonthlyTrend(
    val month: String?,
    val category: String,
    val total_amount: Double
)