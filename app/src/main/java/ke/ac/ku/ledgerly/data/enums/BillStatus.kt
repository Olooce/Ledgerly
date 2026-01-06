package ke.ac.ku.ledgerly.data.enums;

import androidx.room.TypeConverter;

enum class BillStatus {
    PENDING,
    PAID,
    OVERDUE,
    CANCELLED
}

class BillStatusConverter {
    @TypeConverter
    fun fromStatus(status: BillStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): BillStatus = BillStatus.valueOf(value)
}

