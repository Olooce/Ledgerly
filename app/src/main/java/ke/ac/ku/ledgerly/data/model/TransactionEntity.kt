package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val category: String,
    val amount: Double,
    val date: String,
    val type: String, // "Income" or "Expense"
    val notes: String = "",
    val paymentMethod: String = "",
    val tags: String = "" // Comma-separated tags
)