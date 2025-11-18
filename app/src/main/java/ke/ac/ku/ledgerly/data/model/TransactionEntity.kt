package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val category: String,
    val amount: Double,
    val date: Long,
    val type: String,
    val notes: String = "",
    val paymentMethod: String = "",
    val tags: String = "",
    val isDeleted: Boolean = false,
    val lastModified: Long? = System.currentTimeMillis()
)