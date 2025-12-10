package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: Int,
    val color: Long,
    val isDefault: Boolean = false,
    val isDeleted: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val categoryType: String = "Expense" // "Expense" or "Income"
)
