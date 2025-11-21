package ke.ac.ku.ledgerly.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class FirestoreTransaction(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val amount: Double = 0.0,
    val date: Timestamp = Timestamp.now(),
    val category: String = "",
    val notes: String = "",
    val paymentMethod: String = "",
    val tags: String = "",
    val lastModified: Timestamp = Timestamp.now(),
    val deviceId: String = "",
    @PropertyName("deleted")
    val isDeleted: Boolean = false
) {
    companion object {
        fun fromEntity(
            entity: TransactionEntity,
            userId: String,
            deviceId: String
        ): FirestoreTransaction {
            return FirestoreTransaction(
                id = entity.id.toString(),
                userId = userId,
                type = entity.type,
                amount = entity.amount,
                date = Timestamp(entity.date / 1000, 0),
                category = entity.category,
                notes = entity.notes,
                paymentMethod = entity.paymentMethod,
                tags = entity.tags,
                lastModified = Timestamp(
                    (entity.lastModified ?: System.currentTimeMillis()) / 1000,
                    0
                ),
                deviceId = deviceId,
                isDeleted = entity.isDeleted
            )
        }

        fun toEntity(firestoreTransaction: FirestoreTransaction): TransactionEntity {
            return TransactionEntity(
                id = firestoreTransaction.id.toLongOrNull() ?: 0L,
                type = firestoreTransaction.type,
                amount = firestoreTransaction.amount,
                date = firestoreTransaction.date.toDate().time,
                category = firestoreTransaction.category,
                notes = firestoreTransaction.notes,
                paymentMethod = firestoreTransaction.paymentMethod,
                tags = firestoreTransaction.tags,
                lastModified = firestoreTransaction.lastModified.toDate().time,
                isDeleted = firestoreTransaction.isDeleted
            )
        }
    }
}

data class FirestoreRecurringTransaction(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val notes: String = "",
    val paymentMethod: String = "",
    val frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp? = null,
    @PropertyName("active")
    val isActive: Boolean = true,
    val lastModified: Timestamp = Timestamp.now(),
    val deviceId: String = "",
    @PropertyName("deleted")
    val isDeleted: Boolean = false
) {
    companion object {
        fun fromEntity(
            entity: RecurringTransactionEntity,
            userId: String,
            deviceId: String
        ): FirestoreRecurringTransaction {
            return FirestoreRecurringTransaction(
                id = entity.id.toString(),
                userId = userId,
                type = entity.type,
                amount = entity.amount,
                category = entity.category,
                notes = entity.notes,
                paymentMethod = entity.paymentMethod,
                frequency = entity.frequency,
                startDate = Timestamp(entity.startDate / 1000, 0),
                endDate = entity.endDate?.let { Timestamp(it / 1000, 0) },
                isActive = entity.isActive,
                lastModified = Timestamp(
                    (entity.lastModified ?: System.currentTimeMillis()) / 1000,
                    0
                ),
                deviceId = deviceId,
                isDeleted = entity.isDeleted
            )
        }

        fun toEntity(firestoreRecurring: FirestoreRecurringTransaction): RecurringTransactionEntity {
            return RecurringTransactionEntity(
                id = firestoreRecurring.id.toLongOrNull() ?: 0L,
                type = firestoreRecurring.type,
                amount = firestoreRecurring.amount,
                category = firestoreRecurring.category,
                notes = firestoreRecurring.notes,
                paymentMethod = firestoreRecurring.paymentMethod,
                frequency = firestoreRecurring.frequency,
                startDate = firestoreRecurring.startDate.toDate().time,
                endDate = firestoreRecurring.endDate?.toDate()?.time,
                isActive = firestoreRecurring.isActive,
                lastModified = firestoreRecurring.lastModified.toDate().time,
                isDeleted = firestoreRecurring.isDeleted
            )
        }
    }
}

data class FirestoreBudget(
    val category: String = "",
    val userId: String = "",
    val monthYear: String = "",
    val monthlyBudget: Double = 0.0,
    val currentSpending: Double = 0.0,
    val lastModified: Timestamp = Timestamp.now(),
    val deviceId: String = "",
    @PropertyName("deleted")
    val isDeleted: Boolean = false
) {
    companion object {
        fun fromEntity(entity: BudgetEntity, userId: String, deviceId: String): FirestoreBudget {
            return FirestoreBudget(
                category = entity.category,
                userId = userId,
                monthYear = entity.monthYear,
                monthlyBudget = entity.monthlyBudget,
                currentSpending = entity.currentSpending,
                lastModified = Timestamp(
                    (entity.lastModified ?: System.currentTimeMillis()) / 1000,
                    0
                ),
                deviceId = deviceId,
                isDeleted = entity.isDeleted
            )
        }

        fun toEntity(firestoreBudget: FirestoreBudget): BudgetEntity {
            return BudgetEntity(
                category = firestoreBudget.category,
                monthYear = firestoreBudget.monthYear,
                monthlyBudget = firestoreBudget.monthlyBudget,
                currentSpending = firestoreBudget.currentSpending,
                lastModified = firestoreBudget.lastModified.toDate().time,
                isDeleted = firestoreBudget.isDeleted
            )
        }
    }
}