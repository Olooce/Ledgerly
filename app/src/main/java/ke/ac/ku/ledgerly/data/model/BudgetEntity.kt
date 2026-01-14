package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.TypeConverters
import ke.ac.ku.ledgerly.data.converters.Converters
import java.math.BigDecimal
import java.math.RoundingMode

@Entity(tableName = "budgets", primaryKeys = ["category", "monthYear"])
@TypeConverters(Converters::class)
data class BudgetEntity(
    val category: String,
    val monthlyBudget: BigDecimal,
    val currentSpending: BigDecimal = BigDecimal.ZERO,
    val monthYear: String, // Format: "YYYY-MM"
    val isDeleted: Boolean = false,
    val lastModified: Long? = System.currentTimeMillis()
) {
    val remainingBudget: BigDecimal
        get() = this@BudgetEntity.monthlyBudget - currentSpending

    val percentageUsed: BigDecimal
        get() = if (this@BudgetEntity.monthlyBudget > BigDecimal.ZERO)
            currentSpending.multiply(BigDecimal(100))
                .divide(this@BudgetEntity.monthlyBudget, 2, RoundingMode.HALF_UP)
        else BigDecimal.ZERO

    fun isNearLimit(threshold: Int = 80): Boolean {
        return percentageUsed >= BigDecimal(threshold)
    }

    fun isExceeded(): Boolean {
        return currentSpending > this@BudgetEntity.monthlyBudget
    }
}