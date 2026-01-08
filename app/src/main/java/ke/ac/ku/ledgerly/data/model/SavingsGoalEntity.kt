package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ke.ac.ku.ledgerly.data.converters.Converters
import java.math.BigDecimal
import java.math.RoundingMode

@Entity(tableName = "savings_goals")
@TypeConverters(Converters::class)
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal = BigDecimal.ZERO,
    val icon: Int,
    val color: String = "#4CAF50",
    val targetDate: Long? = null,
    val lastMilestoneReached: BigDecimal = BigDecimal.ZERO,
    val createdDate: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false
) {
    val progressPercentage: BigDecimal
        get() = if (targetAmount > BigDecimal.ZERO)
            (currentAmount / targetAmount).setScale(2, RoundingMode.HALF_UP)
                .coerceIn(BigDecimal.ZERO, BigDecimal.ONE) * BigDecimal(100)
            else BigDecimal.ZERO

    val remainingAmount: BigDecimal
        get() = (targetAmount - currentAmount).coerceAtLeast(BigDecimal.ZERO)

    val isOnTrack: Boolean
        get() {
            return if (targetDate == null || isCompleted) true else {
                val totalDays = (targetDate - createdDate) / (1000 * 60 * 60 * 24)
                if (totalDays <= 0) return true  // Goal deadline is in the past or same as creation
                val elapsedDays = (System.currentTimeMillis() - createdDate) / (1000 * 60 * 60 * 24)
                val expectedProgress = (elapsedDays.toBigDecimal() / totalDays.toBigDecimal()) * BigDecimal(100)
                progressPercentage >= expectedProgress
            }
        }

    val daysRemaining: Long?
        get() = if (targetDate == null) null else {
            val remaining = (targetDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
            remaining.coerceAtLeast(0)
        }
}

data class SavingsSummary(
    val totalTarget: BigDecimal?,
    val totalSaved: BigDecimal?
)