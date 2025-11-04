package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val category: String,
    val monthlyBudget: Double,
    val currentSpending: Double = 0.0,
    val monthYear: String // Format: "YYYY-MM"
) {
    val remainingBudget: Double
        get() = monthlyBudget - currentSpending

    val percentageUsed: Double
        get() = if (monthlyBudget > 0) (currentSpending / monthlyBudget) * 100 else 0.0

    fun isNearLimit(threshold: Int = 80): Boolean {
        return percentageUsed >= threshold
    }

    fun isExceeded(): Boolean {
        return currentSpending > monthlyBudget
    }
}