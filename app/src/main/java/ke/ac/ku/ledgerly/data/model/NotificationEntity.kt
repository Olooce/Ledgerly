package ke.ac.ku.ledgerly.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["isRead"]),
        Index(value = ["createdDate"]),
        Index(value = ["isDeleted"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val type: String, // "bill_reminder", "budget_alert", "savings_goal", "debt_reminder", "general"
    val title: String,
    val message: String,
    val relatedId: Long? = null, // ID of related entity (bill, budget, etc.)
    val relatedType: String? = null, // Type of related entity

    val isRead: Boolean = false,
    val isDeleted: Boolean = false,

    val createdDate: Long = System.currentTimeMillis(),
    val readDate: Long? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val icon: Int? = null,
    val actionUrl: String? = null
) {
    val isUnread: Boolean get() = !isRead && !isDeleted
}

data class NotificationSummary(
    val unreadCount: Int = 0,
    val totalCount: Int = 0
)
