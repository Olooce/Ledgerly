package ke.ac.ku.ledgerly.worker

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.RecurrenceFrequency
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dao: TransactionDao
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            processRecurringTransactions()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun processRecurringTransactions() {
        val recurringTransactions = dao.getActiveRecurringTransactions()
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_DATE

        recurringTransactions.forEach { recurring ->
            val startDate = LocalDate.parse(recurring.startDate, formatter)
            val lastGenerated = recurring.lastGeneratedDate?.let {
                LocalDate.parse(it, formatter)
            } ?: startDate.minusDays(1)

            // Check if end date has passed
            val endDate = recurring.endDate?.let { LocalDate.parse(it, formatter) }
            if (endDate != null && today.isAfter(endDate)) {
                // Deactivate expired recurring transaction
                dao.updateRecurringTransactionStatus(recurring.id!!, false)
                return@forEach
            }

            // Calculate next due date
            val nextDueDate = calculateNextDueDate(lastGenerated, recurring.frequency)

            // Generate transactions for all missed dates up to today
            var currentDate = nextDueDate
            while (!currentDate.isAfter(today) && (endDate == null || !currentDate.isAfter(endDate))) {
                // Create transaction
                val transaction = TransactionEntity(
                    id = null,
                    category = recurring.category,
                    amount = recurring.amount,
                    date = currentDate.format(formatter),
                    type = recurring.type,
                    notes = recurring.notes + " (Recurring)",
                    paymentMethod = recurring.paymentMethod,
                    tags = recurring.tags
                )

                dao.insertTransaction(transaction)

                // Update last generated date
                dao.updateRecurringTransaction(
                    recurring.copy(lastGeneratedDate = currentDate.format(formatter))
                )

                // Move to next occurrence
                currentDate = calculateNextDueDate(currentDate, recurring.frequency)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateNextDueDate(
        fromDate: LocalDate,
        frequency: RecurrenceFrequency
    ): LocalDate {
        return when (frequency) {
            RecurrenceFrequency.DAILY -> fromDate.plusDays(1)
            RecurrenceFrequency.WEEKLY -> fromDate.plusWeeks(1)
            RecurrenceFrequency.MONTHLY -> fromDate.plusMonths(1)
            RecurrenceFrequency.YEARLY -> fromDate.plusYears(1)
        }
    }
}