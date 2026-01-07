package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val personName: String,
    val amount: Double,
    val currency: String = "KES",
    val debtType: String, // "owe" = we owe them, "owed" = they owe us
    val dueDate: Long,
    val status: String = "pending", // pending, settled, overdue, partial
    val description: String = "",
    val reminderDays: Int = 0, // Number of days before due date to remind
    val reminderEnabled: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val lastReminderSent: Long = 0
)

data class DebtSummary(
    val totalOwed: Double = 0.0,
    val totalOwe: Double = 0.0,
    val overdueCount: Int = 0,
    val upcomingCount: Int = 0
)

data class DebtWithStatus(
    val debt: DebtEntity,
    val isOverdue: Boolean,
    val daysUntilDue: Int,
    val statusLabel: String
)
