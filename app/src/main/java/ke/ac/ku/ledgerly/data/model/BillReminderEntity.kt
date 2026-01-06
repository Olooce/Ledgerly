package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ke.ac.ku.ledgerly.data.enums.BillStatus

@Entity(
    tableName = "bill_reminders",
    indices = [
        Index(value = ["status"]),
        Index(value = ["dueDate"]),
        Index(value = ["isDeleted"])
    ]
)
data class BillReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,

    val billName: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val dueDate: Long,
    val category: String,
    val frequency: String,
    val nextDueDate: Long?,

    val reminderDays: Int,
    val reminderEnabled: Boolean,
    val notificationSent: Boolean,
    val isOverdue: Boolean,

    val status: String,
    val paymentMethod: String,
    val notes: String,

    val color: Int,

    val createdAt: Long,
    val lastModified: Long,
    val isDeleted: Boolean
) {
    companion object {
        private const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L
    }

    val daysUntilDue: Int
        get() = ((dueDate - System.currentTimeMillis()) / MILLIS_IN_DAY).toInt()

    fun isUpcoming(): Boolean =
        status == BillStatus.PENDING.name &&
                daysUntilDue in 0..reminderDays

    fun needsReminder(): Boolean =
        reminderEnabled &&
                status == BillStatus.PENDING.name &&
                daysUntilDue in 0..reminderDays &&
                !notificationSent
}

data class BillReminderSummary(
    val totalUpcoming: Int = 0,
    val totalAmount: Double = 0.0,
    val overdueCount: Int = 0,
    val thisMonthCount: Int = 0
)
