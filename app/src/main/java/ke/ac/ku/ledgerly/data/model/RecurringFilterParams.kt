package ke.ac.ku.ledgerly.data.model

data class RecurringFilterParams(
    val filterType: String,
    val searchQuery: String,
    val amountRange: ClosedFloatingPointRange<Double>?,
    val categories: List<String>,
    val statusFilter: String?
)
