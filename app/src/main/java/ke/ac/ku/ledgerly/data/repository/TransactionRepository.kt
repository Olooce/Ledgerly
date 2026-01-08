package ke.ac.ku.ledgerly.data.repository

import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.MonthlyTotals
import ke.ac.ku.ledgerly.data.model.PageRequest
import ke.ac.ku.ledgerly.data.model.PaginatedResult
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.domain.CurrencyManager
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val currencyManager: CurrencyManager
) {

    suspend fun addTransaction(transaction: TransactionEntity) {
        // Get the frozen exchange rate at transaction creation time
        val rate = currencyManager.getFrozenExchangeRate(transaction.originalCurrency)
        val amountUsd = transaction.amountOriginal
            .divide(rate, 6, RoundingMode.HALF_UP)
        val newTransaction = transaction.copy(
            exchangeRateToUsd = rate,
            amountUsd = amountUsd
        )
        transactionDao.insertTransaction(newTransaction)
    }

    suspend fun addRecurringTransaction(transaction: RecurringTransactionEntity){
        val rate = currencyManager.getFrozenExchangeRate(transaction.originalCurrency)
        val amountUsd = transaction.amountOriginal
            .divide(rate, 6, RoundingMode.HALF_UP)
        val newTransaction = transaction.copy(
            exchangeRateToUsd = rate,
            amountUsd = amountUsd
        )
        recurringTransactionDao.insertRecurringTransaction(newTransaction)
    }

    suspend fun getFilteredTransactionsPaginated(
        filterType: String,
        searchQuery: String,
        dateRange: String,
        amountRange: ClosedRange<BigDecimal>?,
        categories: List<String>,
        pageRequest: PageRequest
    ): PaginatedResult<TransactionEntity> {

        val dateRangePair = getDateRangeMillis(dateRange)
        val startDate = dateRangePair?.first ?: 0L
        val endDate = dateRangePair?.second ?: System.currentTimeMillis()

        val minAmount = amountRange?.start ?: BigDecimal(-1)
        val maxAmount = amountRange?.endInclusive ?: BigDecimal(-1)

        val transactions = transactionDao.getFilteredTransactionsPaginated(
            filterType = filterType,
            searchQuery = searchQuery,
            dateRange = dateRange,
            startDate = startDate,
            endDate = endDate,
            minAmount = minAmount.toDouble(),
            maxAmount = maxAmount.toDouble(),
            categoriesCount = categories.size,
            categories = categories,
            limit = pageRequest.pageSize,
            offset = (pageRequest.page - 1) * pageRequest.pageSize
        )

        return PaginatedResult(
            data = transactions,
            currentPage = pageRequest.page,
            pageSize = pageRequest.pageSize,
        )
    }

    suspend fun getFilteredRecurringTransactionsPaginated(
        filterType: String,
        searchQuery: String,
        amountRange: ClosedRange<BigDecimal>?,
        categories: List<String>,
        statusFilter: String,
        pageRequest: PageRequest
    ): PaginatedResult<RecurringTransactionEntity> {

        val minAmount = amountRange?.start ?: BigDecimal(-1)
        val maxAmount = amountRange?.endInclusive ?: BigDecimal(-1)

        val transactions = recurringTransactionDao.getFilteredRecurringTransactionsPaginated(
            filterType = filterType,
            searchQuery = searchQuery,
            minAmount = minAmount,
            maxAmount = maxAmount,
            categoriesCount = categories.size,
            categories = categories,
            statusFilter = statusFilter,
            limit = pageRequest.pageSize,
            offset = (pageRequest.page - 1) * pageRequest.pageSize
        )

        return PaginatedResult(
            data = transactions,
            currentPage = pageRequest.page,
            pageSize = pageRequest.pageSize,
        )
    }

    fun getFilteredRecurringTransactionsFlow(
        filterType: String,
        searchQuery: String,
        amountRange: ClosedRange<BigDecimal>?,
        categories: List<String>,
        statusFilter: String
    ): Flow<List<RecurringTransactionEntity>> {
        val minAmount = amountRange?.start ?: BigDecimal(-1)
        val maxAmount = amountRange?.endInclusive ?: BigDecimal(-1)

        return recurringTransactionDao.getFilteredRecurringTransactionsFlow(
            filterType = filterType,
            searchQuery = searchQuery,
            minAmount = minAmount,
            maxAmount = maxAmount,
            categoriesCount = categories.size,
            categories = categories,
            statusFilter = statusFilter
        )
    }

    suspend fun getTransactionsPaginated(pageRequest: PageRequest): PaginatedResult<TransactionEntity> {
        val transactions = transactionDao.getTransactionsPaginated(
            limit = pageRequest.pageSize,
            offset = (pageRequest.page - 1) * pageRequest.pageSize
        )
        return PaginatedResult(
            data = transactions,
            currentPage = pageRequest.page,
            pageSize = pageRequest.pageSize
        )
    }

    suspend fun getTransactionsCount(): Int {
        return transactionDao.getTransactionsCount()
    }

    suspend fun deleteTransaction(transactionId: Long) {
        transactionDao.softDeleteTransaction(transactionId)
    }

    suspend fun getCurrentMonthTransactionsPaginated(pageRequest: PageRequest): PaginatedResult<TransactionEntity> {
        val monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

        val transactions = transactionDao.getTransactionsForMonthPaginated(
            monthYear = monthYear,
            limit = pageRequest.pageSize,
            offset = (pageRequest.page - 1) * pageRequest.pageSize
        )

        return PaginatedResult(
            data = transactions,
            currentPage = pageRequest.page,
            pageSize = pageRequest.pageSize
        )
    }

    suspend fun getCurrentMonthTotals(): MonthlyTotals {
        val monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        return transactionDao.getMonthlyTotals(monthYear) ?: MonthlyTotals(0.0, 0.0)
    }


    private fun getDateRangeMillis(dateRange: String): Pair<Long, Long>? {
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        return when (dateRange) {
            "Today" -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                val end = now
                Pair(start, end)
            }

            "Yesterday" -> {
                calendar.timeInMillis = now
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }

            "Last 7 Days" -> {
                calendar.timeInMillis = now
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                val end = now
                Pair(start, end)
            }

            "Last 30 Days" -> {
                calendar.timeInMillis = now
                calendar.add(Calendar.DAY_OF_YEAR, -29)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                val end = now
                Pair(start, end)
            }

            "Last 90 Days" -> {
                calendar.timeInMillis = now
                calendar.add(Calendar.DAY_OF_YEAR, -89)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                val end = now
                Pair(start, end)
            }

            "Last Year" -> {
                calendar.timeInMillis = now
                calendar.add(Calendar.YEAR, -1)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                val end = now
                Pair(start, end)
            }

            "This Month" -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                val end = now
                Pair(start, end)
            }

            "All Time" -> null
            else -> null
        }
    }

    suspend fun getTransactionForDisplay(transactionId: Long): TransactionEntity? {
        val transaction = transactionDao.getTransactionById(transactionId) ?: return null
        return convertTransactionForDisplay(transaction)
    }

       private suspend fun convertTransactionForDisplay(transaction: TransactionEntity): TransactionEntity {
        val displayCurrency = currencyManager.getDisplayCurrency()
        if (displayCurrency == transaction.originalCurrency) {
            return transaction
        }

        return transaction
    }

     suspend fun getTotalExpenseUsdForMonth(monthYear: String): BigDecimal {
        val transactions = transactionDao.getTransactionsForMonth(monthYear)
            .filter { it.type == "Expense" && !it.isDeleted }
        
        return transactions.fold(BigDecimal.ZERO) { acc, transaction ->
            acc + transaction.amountUsd
        }
    }
    suspend fun getTotalIncomeUsdForMonth(monthYear: String): BigDecimal {
        val transactions = transactionDao.getTransactionsForMonth(monthYear)
            .filter { it.type == "Income" && !it.isDeleted }
        
        return transactions.fold(BigDecimal.ZERO) { acc, transaction ->
            acc + transaction.amountUsd
        }
    }

}
