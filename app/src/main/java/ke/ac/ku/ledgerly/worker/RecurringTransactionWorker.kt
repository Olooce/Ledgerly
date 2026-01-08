package ke.ac.ku.ledgerly.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.enums.RecurrenceFrequency
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.service.NotificationService
import ke.ac.ku.ledgerly.utils.sendRecurringTransactionNotification
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dao: RecurringTransactionDao,
    private val transDao: TransactionDao,
    private val notificationService: NotificationService
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

            var notificationSent = false
            var lastGeneratedDate: LocalDate? = null

            while (!currentDate.isAfter(today) &&
                (endDate == null || !currentDate.isAfter(endDate))
            ) {
                val transaction = TransactionEntity(
                    id = null,
                    category = recurring.category,
                    amountOriginal = recurring.amountOriginal,
                    originalCurrency = recurring.originalCurrency,
                    exchangeRateToUsd = recurring.exchangeRateToUsd,
                    amountUsd = recurring.amountUsd,
                    date = currentDate.toEpochMillis(),
                    type = recurring.type,
                    notes = "${recurring.notes} (Recurring)",
                    paymentMethod = recurring.paymentMethod,
                    tags = recurring.tags
                )

                transDao.insertTransaction(transaction)
                lastGeneratedDate = currentDate

                if (!notificationSent) {
                    notificationService.sendRecurringTransactionNotification(
                        transactionName = recurring.notes,
                        amount = recurring.amountOriginal.toPlainString(),
                        type = recurring.type.lowercase()
                    )
                    notificationSent = true
                }

                currentDate = calculateNextDueDate(currentDate, recurring.frequency)
            }

            if (lastGeneratedDate != null) {
                dao.updateRecurringTransaction(
                    recurring.copy(lastGeneratedDate = lastGeneratedDate.toEpochMillis())
                )
            }
        }
    }

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

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun LocalDate.toEpochMillis(): Long =
        this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
