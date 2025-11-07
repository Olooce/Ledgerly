package ke.ac.ku.ledgerly.data.model

import com.google.firebase.Timestamp

data class FirestoreTransaction(
    val id: String? = null,
    val category: String = "",
    val amount: Double = 0.0,
    val date: Timestamp = Timestamp.now(),
    val type: String = "",
    val notes: String = "",
    val paymentMethod: String = "",
    val tags: List<String> = emptyList(),
    val userId: String = "",
    val lastModified: Timestamp = Timestamp.now(),
    val deviceId: String = ""
) {
    companion object {
        fun fromEntity(entity: TransactionEntity, userId: String, deviceId: String): FirestoreTransaction {
            return FirestoreTransaction(
                id = entity.id?.toString(),
                category = entity.category,
                amount = entity.amount,
                date = Timestamp(entity.date / 1000, 0),
                type = entity.type,
                notes = entity.notes,
                paymentMethod = entity.paymentMethod,
                tags = entity.tags.split(",").filter { it.isNotBlank() },
                userId = userId,
                deviceId = deviceId,
                lastModified = Timestamp((entity.lastModified ?: System.currentTimeMillis()) / 1000, 0)
            )
        }

        fun toEntity(firestoreTransaction: FirestoreTransaction): TransactionEntity {
            return TransactionEntity(
                id = firestoreTransaction.id?.toLongOrNull(),
                category = firestoreTransaction.category,
                amount = firestoreTransaction.amount,
                date = firestoreTransaction.date.seconds * 1000,
                type = firestoreTransaction.type,
                notes = firestoreTransaction.notes,
                paymentMethod = firestoreTransaction.paymentMethod,
                tags = firestoreTransaction.tags.joinToString(","),
                lastModified = firestoreTransaction.lastModified.seconds * 1000
            )
        }
    }
}

data class FirestoreBudget(
    val category: String = "",
    val monthlyBudget: Double = 0.0,
    val currentSpending: Double = 0.0,
    val monthYear: String = "",
    val userId: String = "",
    val lastModified: Timestamp = Timestamp.now(),
    val deviceId: String = ""
) {
    companion object {
        fun fromEntity(entity: BudgetEntity, userId: String, deviceId: String): FirestoreBudget {
            return FirestoreBudget(
                category = entity.category,
                monthlyBudget = entity.monthlyBudget,
                currentSpending = entity.currentSpending,
                monthYear = entity.monthYear,
                userId = userId,
                deviceId = deviceId,
                lastModified = Timestamp((entity.lastModified ?: System.currentTimeMillis()) / 1000, 0)
            )
        }

        fun toEntity(firestoreBudget: FirestoreBudget): BudgetEntity {
            return BudgetEntity(
                category = firestoreBudget.category,
                monthlyBudget = firestoreBudget.monthlyBudget,
                currentSpending = firestoreBudget.currentSpending,
                monthYear = firestoreBudget.monthYear,
                lastModified = firestoreBudget.lastModified.seconds * 1000
            )
        }
    }
}

data class FirestoreRecurringTransaction(
    val id: String? = null,
    val category: String = "",
    val amount: Double = 0.0,
    val type: String = "",
    val notes: String = "",
    val paymentMethod: String = "",
    val tags: List<String> = emptyList(),
    val frequency: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp? = null,
    val lastGeneratedDate: Timestamp? = null,
    val isActive: Boolean = true,
    val userId: String = "",
    val lastModified: Timestamp = Timestamp.now(),
    val deviceId: String = ""
) {
    companion object {
        fun fromEntity(entity: RecurringTransactionEntity, userId: String, deviceId: String): FirestoreRecurringTransaction {
            return FirestoreRecurringTransaction(
                id = entity.id?.toString(),
                category = entity.category,
                amount = entity.amount,
                type = entity.type,
                notes = entity.notes,
                paymentMethod = entity.paymentMethod,
                tags = entity.tags.split(",").filter { it.isNotBlank() },
                frequency = entity.frequency.name,
                startDate = Timestamp(entity.startDate / 1000, 0),
                endDate = entity.endDate?.let { Timestamp(it / 1000, 0) },
                lastGeneratedDate = entity.lastGeneratedDate?.let { Timestamp(it / 1000, 0) },
                isActive = entity.isActive,
                userId = userId,
                deviceId = deviceId,
                lastModified = Timestamp((entity.lastModified ?: System.currentTimeMillis()) / 1000, 0)
            )
        }

        fun toEntity(firestoreTransaction: FirestoreRecurringTransaction): RecurringTransactionEntity {
            return RecurringTransactionEntity(
                id = firestoreTransaction.id?.toLongOrNull(),
                category = firestoreTransaction.category,
                amount = firestoreTransaction.amount,
                type = firestoreTransaction.type,
                notes = firestoreTransaction.notes,
                paymentMethod = firestoreTransaction.paymentMethod,
                tags = firestoreTransaction.tags.joinToString(","),
                frequency = RecurrenceFrequency.valueOf(firestoreTransaction.frequency),
                startDate = firestoreTransaction.startDate.seconds * 1000,
                endDate = firestoreTransaction.endDate?.seconds?.times(1000),
                lastGeneratedDate = firestoreTransaction.lastGeneratedDate?.seconds?.times(1000),
                isActive = firestoreTransaction.isActive,
                lastModified = firestoreTransaction.lastModified.seconds * 1000
            )
        }
    }
}