package ke.ac.ku.ledgerly.presentation.stats

import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.LargeValueFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.BaseViewModel
import ke.ac.ku.ledgerly.base.UiEvent
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.enums.TimePeriod
import ke.ac.ku.ledgerly.data.model.CategorySummary
import ke.ac.ku.ledgerly.data.model.MonthlyComparison
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionSummary
import ke.ac.ku.ledgerly.utils.FormatingUtils
import ke.ac.ku.ledgerly.utils.Utils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    val dao: TransactionDao
) : BaseViewModel() {

    // Get date range for the selected period
    private fun getDateRangeForPeriod(period: TimePeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        return when (period) {
            TimePeriod.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis to endDate
            }

            TimePeriod.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis to endDate
            }

            TimePeriod.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis to endDate
            }
        }
    }

    fun getEntriesForPeriod(period: TimePeriod): Flow<List<TransactionSummary>> {
        val (startDate, endDate) = getDateRangeForPeriod(period)
        return dao.getExpensesByDateForPeriod(startDate, endDate)
    }

    fun getTopEntriesForPeriod(period: TimePeriod, limit: Int = 5): Flow<List<TransactionEntity>> {
        val (startDate, endDate) = getDateRangeForPeriod(period)
        return dao.getTopExpensesForPeriod(startDate, endDate, limit)
    }

    fun getCategorySpendingForPeriod(period: TimePeriod): Flow<List<CategorySummary>> {
        val (startDate, endDate) = getDateRangeForPeriod(period)

        return when (period) {
            TimePeriod.MONTH -> {
                dao.getExpenseByCategoryForMonth(Utils.getCurrentMonthYear())
            }

            else -> {
                dao.getMonthlySpendingTrends().map { trends ->
                    trends
                        .filter { trend ->
                            val trendDate = parseMonthYearToMillis(trend.month ?: "")
                            trendDate in startDate..endDate
                        }
                        .groupBy { it.category }
                        .map { (category, list) ->
                            CategorySummary(
                                category = category,
                                total_amount = list.sumOf { it.total_amount }
                            )
                        }
                        .filter { it.total_amount > 0 }
                }
            }
        }
    }

    fun getComparisonForPeriod(period: TimePeriod): Flow<List<MonthlyComparison>> {
        val (startDate, endDate) = getDateRangeForPeriod(period)

        return when (period) {
            TimePeriod.WEEK -> {
                dao.getDailyComparisonForPeriod(startDate, endDate)
            }

            TimePeriod.MONTH -> {
                dao.getMonthlyComparisonForPeriod(startDate, endDate).map { list ->
                    if (list.isEmpty()) list else list.takeLast(1)
                }
            }

            TimePeriod.YEAR -> {
                dao.getMonthlyComparisonForPeriod(startDate, endDate)
            }
        }
    }

    fun getEntriesForChart(entries: List<TransactionSummary>): List<Entry> {
        return entries
            .sortedBy { it.date }
            .map {
                val millis = it.date
                Entry(millis.toFloat(), it.total_amount.toFloat())
            }
    }

    fun getFilteredMonthlyData(monthlyData: List<MonthlyComparison>): List<MonthlyComparison> {
        return monthlyData.filter { it.month != null }
    }

    fun getBarChartData(monthlyData: List<MonthlyComparison>): BarData? {
        val filteredData = getFilteredMonthlyData(monthlyData)
        if (filteredData.isEmpty()) return null

        val incomeEntries = mutableListOf<BarEntry>()
        val expenseEntries = mutableListOf<BarEntry>()

        filteredData.forEachIndexed { index, data ->
            incomeEntries.add(BarEntry(index.toFloat(), data.income.toFloat()))
            expenseEntries.add(BarEntry(index.toFloat(), data.expense.toFloat()))
        }

        val incomeSet = BarDataSet(incomeEntries, "Income").apply {
            color = "#FF4CAF50".toColorInt()
            valueTextColor = android.graphics.Color.BLACK
            valueTextSize = 12f
        }

        val expenseSet = BarDataSet(expenseEntries, "Expense").apply {
            color = "#FFF44336".toColorInt()
            valueTextColor = android.graphics.Color.BLACK
            valueTextSize = 12f
        }

        return BarData(incomeSet, expenseSet).apply {
            barWidth = 0.3f
            setValueFormatter(LargeValueFormatter())
        }
    }

    fun getLabelsForPeriod(
        monthlyData: List<MonthlyComparison>,
        period: TimePeriod
    ): List<String> {
        val filteredData = getFilteredMonthlyData(monthlyData)

        return when (period) {
            TimePeriod.WEEK -> {
                filteredData.mapIndexed { index, _ -> "Day ${index + 1}" }
            }

            TimePeriod.MONTH -> {
                filteredData.map {
                    FormatingUtils.formatMonthString(it.month!!)
                }
            }

            TimePeriod.YEAR -> {
                filteredData.map {
                    val parts = it.month?.split("-")
                    if (parts != null && parts.size == 2) {
                        val monthNum = parts[1].toIntOrNull() ?: 1
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.MONTH, monthNum - 1)
                        SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
                    } else {
                        FormatingUtils.formatMonthString(it.month!!)
                    }
                }
            }
        }
    }

    private fun parseMonthYearToMillis(monthYear: String): Long {
        return try {
            val parts = monthYear.split("-")
            if (parts.size == 2) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val calendar = Calendar.getInstance()
                calendar.set(year, month - 1, 1, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    fun getIncomeForPeriod(period: TimePeriod): Flow<List<TransactionSummary>> {
        val (startDate, endDate) = getDateRangeForPeriod(period)
        return dao.getIncomeByDateForPeriod(startDate, endDate)
    }

    override fun onEvent(event: UiEvent) {
    }
}
