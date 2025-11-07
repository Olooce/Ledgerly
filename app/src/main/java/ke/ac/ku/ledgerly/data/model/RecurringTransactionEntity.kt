package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val category: String,
    val amount: Double,
    val type: String,
    val notes: String = "",
    val paymentMethod: String = "",
    val tags: String = "",
    val frequency: RecurrenceFrequency,
    val startDate: Long,
    val endDate: Long? = null,
    val lastGeneratedDate: Long? = null,
    val isActive: Boolean = true
)

enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}


class Converters {
    @TypeConverter
    fun fromRecurrenceFrequency(value: RecurrenceFrequency): String {
        return value.name
    }

    @TypeConverter
    fun toRecurrenceFrequency(value: String): RecurrenceFrequency {
        return RecurrenceFrequency.valueOf(value)
    }
}