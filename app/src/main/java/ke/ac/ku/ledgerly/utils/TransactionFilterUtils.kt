package ke.ac.ku.ledgerly.utils

import ke.ac.ku.ledgerly.data.model.TransactionEntity
import java.util.Calendar

object TransactionFilterUtils {

    fun filterTransactions(
        transactions: List<TransactionEntity>,
        filterType: String,
        dateRange: String,
        searchQuery: String = "",
        amountRange: ClosedFloatingPointRange<Double>? = null,
        categories: List<String> = emptyList()
    ): List<TransactionEntity> {
        return transactions
            .filterByType(filterType)
            .filterByDateRange(dateRange)
            .filterBySearchQuery(searchQuery)
            .filter { tx ->
                val matchesAmount = amountRange?.let { tx.amount in it } ?: true
                val matchesCategory = categories.isEmpty() || categories.contains(tx.category)
                matchesAmount && matchesCategory
            }
    }

    private fun List<TransactionEntity>.filterByType(filterType: String): List<TransactionEntity> {
        return when (filterType) {
            "Expense" -> this.filter { it.type.equals("Expense", true) }
            "Income" -> this.filter { it.type.equals("Income", true) }
            else -> this
        }
    }

    private fun List<TransactionEntity>.filterByDateRange(dateRange: String): List<TransactionEntity> {
        val calendar = Calendar.getInstance()
        val currentTime = System.currentTimeMillis()

        return when (dateRange) {
            "Today" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                this.filter { it.date in startOfDay..currentTime }
            }
            "Yesterday" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfYesterday = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfYesterday = calendar.timeInMillis
                this.filter { it.date in startOfYesterday..endOfYesterday }
            }
            "Last 7 Days" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val sevenDaysAgo = calendar.timeInMillis
                this.filter { it.date >= sevenDaysAgo }
            }
            "Last 30 Days" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val thirtyDaysAgo = calendar.timeInMillis
                this.filter { it.date >= thirtyDaysAgo }
            }
            "Last 90 Days" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -90)
                val ninetyDaysAgo = calendar.timeInMillis
                this.filter { it.date >= ninetyDaysAgo }
            }
            "Last Year" -> {
                calendar.add(Calendar.YEAR, -1)
                val oneYearAgo = calendar.timeInMillis
                this.filter { it.date >= oneYearAgo }
            }
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                this.filter { it.date >= startOfMonth }
            }
            else -> this // "All Time"
        }
    }

    private fun List<TransactionEntity>.filterBySearchQuery(query: String): List<TransactionEntity> {
        return if (query.isBlank()) this
        else this.filter {
            it.category.contains(query, true) ||
                    it.notes?.contains(query, true) == true ||
                    it.paymentMethod.contains(query, true) ||
                    it.tags.split(",").any { tag -> tag.contains(query, ignoreCase = true) }
        }
    }
}
