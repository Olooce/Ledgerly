package ke.ac.ku.ledgerly.data.model

data class TransactionSummary(
    val type: String,
    val date: String,
    val total_amount: Double
)