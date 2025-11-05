package ke.ac.ku.ledgerly.feature.stats

import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.LargeValueFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.BaseViewModel
import ke.ac.ku.ledgerly.base.UiEvent
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.CategorySummary
import ke.ac.ku.ledgerly.data.model.MonthlyComparison
import ke.ac.ku.ledgerly.data.model.TransactionSummary
import ke.ac.ku.ledgerly.utils.Utils
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(val dao: TransactionDao) : BaseViewModel() {
    val entries = dao.getAllExpenseByDate()
    val topEntries = dao.getTopExpenses()

    val categorySpending = dao.getExpenseByCategoryForMonth(Utils.getCurrentMonthYear())
    val monthlyComparison = dao.getMonthlyIncomeVsExpense()
    val spendingTrends = dao.getMonthlySpendingTrends()

    fun getEntriesForChart(entries: List<TransactionSummary>): List<Entry> {
        return entries
            .sortedBy { Utils.getMillisFromDate(it.date) }
            .map {
                val millis = Utils.getMillisFromDate(it.date)
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

    fun getMonthLabels(monthlyData: List<MonthlyComparison>): List<String> {
        return getFilteredMonthlyData(monthlyData).map {
            Utils.formatMonthString(it.month!!)
        }
    }

    override fun onEvent(event: UiEvent) {
    }
}
