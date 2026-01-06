package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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
    val daysUntilDue: Int
        get() {
            // Calculate days considering local time zone
            val now = System.currentTimeMillis()
            val diff = dueDate - now
            return (diff / (1000 * 60 * 60 * 24)).toInt()
        }

    fun isUpcoming(): Boolean =
        daysUntilDue in 0..reminderDays && status == "pending"

    fun needsReminder(): Boolean =
        reminderEnabled &&
                daysUntilDue <= reminderDays &&
                !notificationSent &&
                status == "pending"
}


data class BillReminderSummary(
    val totalUpcoming: Int = 0,
    val totalAmount: Double = 0.0,
    val overdueCount: Int = 0,
    val thisMonthCount: Int = 0
)
