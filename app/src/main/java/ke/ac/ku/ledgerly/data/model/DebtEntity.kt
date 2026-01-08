package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ke.ac.ku.ledgerly.data.converters.Converters
import java.math.BigDecimal

@Entity(tableName = "debts")
@TypeConverters(Converters::class)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val personName: String,
    val amountOriginal: BigDecimal,
    val originalCurrency: String,
    val exchangeRateToUsd: BigDecimal,
    val amount: BigDecimal,
    val debtType: String, // "owe" = we owe them, "owed" = they owe us
    val dueDate: Long,
    val status: String = "pending", // pending, settled, overdue, partial
    val description: String = "",
    val reminderDays: Int = 0, // Number of days before due date to remind
    val reminderEnabled: Boolean = true,
    val reminderSent: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val lastReminderSent: Long = 0
)

data class DebtSummary(
    val totalOwed: BigDecimal = BigDecimal.ZERO,
    val totalOwe: BigDecimal = BigDecimal.ZERO,
    val overdueCount: Int = 0,
    val upcomingCount: Int = 0
)

data class DebtWithStatus(
    val debt: DebtEntity,
    val isOverdue: Boolean,
    val daysUntilDue: Int,
    val statusLabel: String
)
