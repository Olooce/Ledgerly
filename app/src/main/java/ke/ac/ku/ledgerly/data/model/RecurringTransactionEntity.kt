package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ke.ac.ku.ledgerly.data.converters.Converters
import ke.ac.ku.ledgerly.data.enums.RecurrenceFrequency
import java.math.BigDecimal

@Entity(tableName = "recurring_transactions")
@TypeConverters(Converters::class)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val category: String,
    val amountOriginal: BigDecimal,
    val originalCurrency: String,
    val exchangeRateToUsd: BigDecimal,
    val amountUsd: BigDecimal,
    val type: String,
    val notes: String = "",
    val paymentMethod: String = "",
    val tags: String = "",
    val frequency: RecurrenceFrequency,
    val startDate: Long,
    val endDate: Long? = null,
    val lastGeneratedDate: Long? = null,
    val isActive: Boolean = true,
    val isDeleted: Boolean = false,
    val lastModified: Long? = System.currentTimeMillis()
)
