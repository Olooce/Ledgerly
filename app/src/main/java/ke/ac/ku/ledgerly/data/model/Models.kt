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

data class MonthlyTotals(
    val totalIncome: Double?,
    val totalExpense: Double?
)


data class ExchangeRateResponse(
    val result: String,
    val base_code: String,
    val rates: Map<String, Double>
)

