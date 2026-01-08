package ke.ac.ku.ledgerly.data.converters

import androidx.room.TypeConverter
import ke.ac.ku.ledgerly.data.enums.RecurrenceFrequency
import java.math.BigDecimal

class Converters {
    @TypeConverter
    fun fromRecurrenceFrequency(value: RecurrenceFrequency): String {
        return value.name
    }

    @TypeConverter
    fun toRecurrenceFrequency(value: String): RecurrenceFrequency {
        return RecurrenceFrequency.valueOf(value)
    }

    @TypeConverter
    fun fromBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun toBigDecimal(value: BigDecimal?): String? {
        return value?.toPlainString()
    }
}
