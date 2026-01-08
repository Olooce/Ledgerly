
package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ke.ac.ku.ledgerly.data.converters.Converters
import java.math.BigDecimal

@Entity(tableName = "transactions")
@TypeConverters(Converters::class)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val category: String,
    val amountOriginal: BigDecimal,
    val originalCurrency: String,
    val exchangeRateToUsd: BigDecimal,
    val amountUsd: BigDecimal,
    val date: Long,
    val type: String,
    val notes: String = "",
    val paymentMethod: String = "",
    val tags: String = "",
    val isDeleted: Boolean = false,
    val lastModified: Long? = System.currentTimeMillis()
)
