package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val icon: Int,
    val color: String = "#4CAF50",
    val targetDate: Long? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false
) {
    val progressPercentage: Double
        get() = if (targetAmount > 0) (currentAmount / targetAmount).coerceIn(0.0, 1.0) * 100 else 0.0

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)

    val isOnTrack: Boolean
        get() {
            return if (targetDate == null || isCompleted) true else {
                val totalDays = (targetDate - createdDate) / (1000 * 60 * 60 * 24)
                if (totalDays <= 0) return true  // Goal deadline is in the past or same as creation
                val elapsedDays = (System.currentTimeMillis() - createdDate) / (1000 * 60 * 60 * 24)
                val expectedProgress = (elapsedDays / totalDays) * 100
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
    val totalTarget: Double?,
    val totalSaved: Double?
)
