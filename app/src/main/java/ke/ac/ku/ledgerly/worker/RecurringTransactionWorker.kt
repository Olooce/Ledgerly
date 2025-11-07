package ke.ac.ku.ledgerly.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.RecurrenceFrequency
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dao: TransactionDao
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            processRecurringTransactions()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun processRecurringTransactions() {
        val recurringTransactions = dao.getActiveRecurringTransactions()
        val today = LocalDate.now()

        recurringTransactions.forEach { recurring ->
            val startDate = recurring.startDate.toLocalDate()
            val lastGenerated = recurring.lastGeneratedDate?.toLocalDate() ?: startDate.minusDays(1)
            val endDate = recurring.endDate?.toLocalDate()

            // Stop if end date passed
            if (endDate != null && today.isAfter(endDate)) {
                dao.updateRecurringTransactionStatus(recurring.id!!, false)
                return@forEach
            }

            // Generate next transactions until current date
            var currentDate = calculateNextDueDate(lastGenerated, recurring.frequency)
            while (!currentDate.isAfter(today) && (endDate == null || !currentDate.isAfter(endDate))) {
                val transaction = TransactionEntity(
                    id = null,
                    category = recurring.category,
                    amount = recurring.amount,
                    date = currentDate.toEpochMillis(),
                    type = recurring.type,
                    notes = "${recurring.notes} (Recurring)",
                    paymentMethod = recurring.paymentMethod,
                    tags = recurring.tags
                )

                dao.insertTransaction(transaction)
                dao.updateRecurringTransaction(
                    recurring.copy(lastGeneratedDate = currentDate.toEpochMillis())
                )

                currentDate = calculateNextDueDate(currentDate, recurring.frequency)
            }
        }
    }

    private fun calculateNextDueDate(fromDate: LocalDate, frequency: RecurrenceFrequency): LocalDate {
        return when (frequency) {
            RecurrenceFrequency.DAILY -> fromDate.plusDays(1)
            RecurrenceFrequency.WEEKLY -> fromDate.plusWeeks(1)
            RecurrenceFrequency.MONTHLY -> fromDate.plusMonths(1)
            RecurrenceFrequency.YEARLY -> fromDate.plusYears(1)
        }
    }

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun LocalDate.toEpochMillis(): Long =
        this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
