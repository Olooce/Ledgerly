package ke.ac.ku.ledgerly.data.repository

import PageRequest
import PaginatedResult
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.MonthlyTotals
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
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

    suspend fun getFilteredTransactionsPaginated(
        filterType: String,
        searchQuery: String,
        pageRequest: PageRequest
    ): PaginatedResult<TransactionEntity> {
        val transactions = transactionDao.getFilteredTransactionsPaginated(
            filterType = filterType,
            searchQuery = searchQuery,
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

    suspend fun getFilteredTransactionsCount(filterType: String, searchQuery: String): Int {
        return transactionDao.getFilteredTransactionsCount(filterType, searchQuery)
    }
    suspend fun deleteTransaction(transactionId: Long) {
        transactionDao.softDeleteTransaction(transactionId)
    }

    suspend fun getCurrentMonthTransactionsPaginated(pageRequest: PageRequest): PaginatedResult<TransactionEntity> {
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

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
        val monthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        return transactionDao.getMonthlyTotals(monthYear) ?: MonthlyTotals(0.0, 0.0)
    }

}