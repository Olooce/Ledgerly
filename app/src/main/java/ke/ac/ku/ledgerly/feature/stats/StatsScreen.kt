package ke.ac.ku.ledgerly.feature.stats

import android.content.Context
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.model.CategorySummary
import ke.ac.ku.ledgerly.data.model.MonthlyComparison
import ke.ac.ku.ledgerly.feature.home.TransactionList
import ke.ac.ku.ledgerly.utils.Utils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Trends", "Categories", "Comparison")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu action */ }) {
                        Icon(
                            painter = painterResource(R.drawable.dots_menu),
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Animated content
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally { it }.togetherWith(slideOutHorizontally { -it })
                },
                label = "tab_animation"
            ) { index ->
                when (index) {
                    0 -> TrendsTab(viewModel, navController)
                    1 -> CategoriesTab(viewModel)
                    2 -> ComparisonTab(viewModel)
                }
            }
        }
    }
}

@Composable
private fun TrendsTab(viewModel: StatsViewModel, navController: NavController) {
    val dataState by viewModel.entries.collectAsState(emptyList())
    val topExpenses by viewModel.topEntries.collectAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatsCard {
            Column {
                Text(
                    "Spending Over Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (dataState.isNotEmpty()) {
                    LineChartView(entries = viewModel.getEntriesForChart(dataState))
                } else {
                    EmptyState("No transaction data available")
                }
            }
        }

        if (topExpenses.isNotEmpty()) {
            StatsCard {
                TransactionList(
                    modifier = Modifier,
                    list = topExpenses,
                    title = "Top Spending",
                    onSeeAllClicked = {
                        navController.navigate("all_transactions")
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoriesTab(viewModel: StatsViewModel) {
    val categoryData by viewModel.categorySpending.collectAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        StatsCard {
            Column {
                Text(
                    "Spending by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (categoryData.isNotEmpty()) {
                    PieChartView(categorySummaries = categoryData)
                } else {
                    EmptyState("No category data for this month")
                }
            }
        }
    }
}

@Composable
private fun ComparisonTab(viewModel: StatsViewModel) {
    val monthlyData by viewModel.monthlyComparison.collectAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary cards
        val filtered = monthlyData.filter { it.month != null }
        if (filtered.isNotEmpty()) {
            val latest = filtered.last()
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SummaryCard(
                    title = "Income",
                    value = Utils.formatCurrency(latest.income),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
                SummaryCard(
                    title = "Expenses",
                    value = Utils.formatCurrency(latest.expense),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        StatsCard {
            Column {
                Text(
                    "Monthly Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (monthlyData.isNotEmpty()) {
                    BarChartView(
                        monthlyData = monthlyData,
                        viewModel = viewModel
                    )
                } else {
                    EmptyState("No monthly comparison data")
                }
            }
        }
    }
}

@Composable
private fun StatsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LineChartView(entries: List<Entry>) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5
    val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        factory = {
            LayoutInflater.from(context)
                .inflate(R.layout.stats_line_chart, null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) { view ->
        val chart = view.findViewById<LineChart>(R.id.lineChart)

        val dataSet = LineDataSet(entries, "Expenses").apply {
            color = primaryColor
            lineWidth = 3f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(context, R.drawable.char_gradient)
            valueTextSize = 12f
            valueTextColor = primaryColor
            setDrawCircles(false)
        }

        chart.apply {
            xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) =
                        Utils.formatDateForChart(value.toLong())
                }
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(false)
            }
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            legend.textColor = textColor
            description.isEnabled = false
            setBackgroundColor(android.graphics.Color.TRANSPARENT)

            data = LineData(dataSet)
            animateY(1200, Easing.EaseInOutQuad)
            invalidate()
        }
    }
}

@Composable
private fun PieChartView(categorySummaries: List<CategorySummary>) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5
    val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK

    AndroidView(
        factory = {
            LayoutInflater.from(context)
                .inflate(R.layout.stats_pie_chart, null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) { view ->
        val chart = view.findViewById<com.github.mikephil.charting.charts.PieChart>(R.id.pieChart)

        val entries = categorySummaries.map {
            PieEntry(it.total_amount.toFloat(), it.category)
        }

        val dataSet = PieDataSet(entries, "Category Spending").apply {
            colors = getThemeColors(context)
            valueTextColor = textColor
            valueTextSize = 12f
            sliceSpace = 3f
            selectionShift = 5f
        }

        chart.apply {
            data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            legend.textColor = textColor
            setEntryLabelColor(textColor)
            setEntryLabelTextSize(12f)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }
}

@Composable
private fun BarChartView(
    monthlyData: List<MonthlyComparison>,
    viewModel: StatsViewModel
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5
    val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK

    AndroidView(
        factory = {
            LayoutInflater.from(context)
                .inflate(R.layout.stats_bar_chart, null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) { view ->
        val chart = view.findViewById<BarChart>(R.id.barChart)
        val barData = viewModel.getBarChartData(monthlyData)

        barData?.let {
            chart.apply {
                xAxis.apply {
                    valueFormatter = object : ValueFormatter() {
                        val labels = viewModel.getMonthLabels(monthlyData)
                        override fun getFormattedValue(value: Float) =
                            labels.getOrNull(value.toInt()) ?: ""
                    }
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                }
                axisLeft.apply {
                    setDrawGridLines(true)
                }
                axisRight.isEnabled = false
                legend.textColor = textColor
                description.isEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                data = barData
                groupBars(0f, 0.4f, 0f)
                animateY(1200, Easing.EaseInOutQuad)
                invalidate()
            }
        }
    }
}

private fun getThemeColors(context: Context): List<Int> = listOf(
    R.color.color1, R.color.color2, R.color.color3,
    R.color.color4, R.color.color5, R.color.color6
).map { ContextCompat.getColor(context, it) }