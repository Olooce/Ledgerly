package ke.ac.ku.ledgerly.presentation.stats

import android.content.Context
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.enums.TimePeriod
import ke.ac.ku.ledgerly.data.model.CategorySummary
import ke.ac.ku.ledgerly.data.model.MonthlyComparison
import ke.ac.ku.ledgerly.ui.components.TransactionList
import ke.ac.ku.ledgerly.ui.theme.White
import ke.ac.ku.ledgerly.utils.FormatingUtils
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPeriod by remember { mutableStateOf(TimePeriod.MONTH) }
    val tabs = listOf("Comparison", "Trends", "Categories")

    Box(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (topBar) = createRefs()
            Image(
                painter = painterResource(R.drawable.ic_topbar),
                contentDescription = "TopBar",
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
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
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = padding.calculateTopPadding(),
                            start = 2.dp,
                            end = 2.dp
                        )
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
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

                    // Period Filter
                    PeriodFilterRow(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it }
                    )

                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            slideInHorizontally { it }.togetherWith(slideOutHorizontally { -it })
                        },
                        label = "tab_animation",
                        modifier = Modifier.fillMaxSize()
                    ) { index ->
                        when (index) {
                            0 -> ComparisonTab(viewModel, selectedPeriod)
                            1 -> TrendsTab(viewModel, navController, selectedPeriod)
                            2 -> CategoriesTab(viewModel, selectedPeriod)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodFilterRow(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        TimePeriod.entries.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}


@Composable
private fun TrendsTab(
    viewModel: StatsViewModel,
    navController: NavController,
    selectedPeriod: TimePeriod
) {
    val dataState by viewModel.getEntriesForPeriod(selectedPeriod).collectAsState(emptyList())
    val topExpenses by viewModel.getTopEntriesForPeriod(selectedPeriod).collectAsState(emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Spending Over Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (dataState.isNotEmpty()) {
                    LineChartView(entries = viewModel.getEntriesForChart(dataState))
                } else {
                    EmptyState("No transaction data available")
                }
            }
        }

        if (topExpenses.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                ) {
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
}

@Composable
private fun CategoriesTab(viewModel: StatsViewModel, selectedPeriod: TimePeriod) {
    val categoryData by viewModel.getCategorySpendingForPeriod(selectedPeriod)
        .collectAsState(emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            if (categoryData.isNotEmpty()) {
                PieChartView(categorySummaries = categoryData)
            } else {
                EmptyState("No category data for this period")
            }
        }
    }
}

@Composable
private fun ComparisonTab(viewModel: StatsViewModel, selectedPeriod: TimePeriod) {
    val monthlyData by viewModel.getComparisonForPeriod(selectedPeriod).collectAsState(emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Summary cards
        item {
            val filtered = monthlyData.filter { it.month != null }
            if (filtered.isNotEmpty()) {
                val totalIncome = filtered.sumOf { it.income }
                val totalExpense = filtered.sumOf { it.expense }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SummaryCard(
                        title = "Income",
                        value = FormatingUtils.formatCurrency(totalIncome),
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF2E7D32),
                    )
                    SummaryCard(
                        title = "Expenses",
                        value = FormatingUtils.formatCurrency(totalExpense),
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFC62828)
                    )
                }
            }
        }

        // Comparison Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "${selectedPeriod.name.lowercase().capitalize(Locale.ROOT)} Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (monthlyData.isNotEmpty()) {
                    BarChartView(
                        monthlyData = monthlyData,
                        viewModel = viewModel,
                        selectedPeriod = selectedPeriod
                    )
                } else {
                    EmptyState("No comparison data")
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = White.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_empty_state),
                contentDescription = "No Data",
                modifier = Modifier.size(120.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LineChartView(entries: List<Entry>) {
    val context = LocalContext.current
    val valTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val valGridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f).toArgb()
    val primaryColor = MaterialTheme.colorScheme.inversePrimary.toArgb()

    AndroidView(
        factory = {
            LayoutInflater.from(context)
                .inflate(R.layout.stats_line_chart, null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) { view ->
        val chart = view.findViewById<LineChart>(R.id.lineChart)

        val dataSet = LineDataSet(entries, "Expenses").apply {
            color = primaryColor
            lineWidth = 3f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextSize = 11f
            valueTextColor = valTextColor
            setDrawCircles(true)
            circleRadius = 4f
            setCircleColor(primaryColor)
            circleHoleRadius = 2f
            setDrawCircleHole(true)
        }

        chart.apply {
            xAxis.apply {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) =
                        FormatingUtils.formatDateForChart(value.toLong())
                }
                position = XAxis.XAxisPosition.BOTTOM
                textColor = valTextColor
                gridColor = valGridColor
                setDrawGridLines(true)
                setDrawAxisLine(false)
                textSize = 10f
            }
            axisLeft.apply {
                textColor = valTextColor
                gridColor = valGridColor
                setDrawGridLines(true)
                setDrawAxisLine(false)
                textSize = 10f
            }
            axisRight.isEnabled = false
            legend.textColor = valTextColor
            description.isEnabled = false
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setDrawBorders(false)
            extraBottomOffset = 10f
            extraTopOffset = 10f

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
    val valTextColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY

    AndroidView(
        factory = {
            LayoutInflater.from(context)
                .inflate(R.layout.stats_pie_chart, null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    ) { view ->
        val chart = view.findViewById<PieChart>(R.id.pieChart)

        val entries = categorySummaries.map {
            PieEntry(it.total_amount.toFloat(), it.category)
        }

        val dataSet = PieDataSet(entries, "Category Spending").apply {
            colors = getColors(context, entries.size)
            valueTextColor = valTextColor
            valueTextSize = 13f
            sliceSpace = 2f
            selectionShift = 7f
        }

        chart.apply {
            data = PieData(dataSet)
            setUsePercentValues(true)
            description.isEnabled = false
            legend.apply {
                this.textColor = valTextColor
                textSize = 12f
            }
            setEntryLabelColor(valTextColor)
            setEntryLabelTextSize(13f)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            isDrawHoleEnabled = false
            setTransparentCircleAlpha(0)
            centerText = "Categories"
            setCenterTextColor(valTextColor)
            setCenterTextSize(14f)
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }
}

@Composable
private fun BarChartView(
    monthlyData: List<MonthlyComparison>,
    viewModel: StatsViewModel,
    selectedPeriod: TimePeriod
) {
    val context = LocalContext.current
    val valTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val valGridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f).toArgb()

    AndroidView(
        factory = {
            LayoutInflater.from(context)
                .inflate(R.layout.stats_bar_chart, null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) { view ->
        val chart = view.findViewById<BarChart>(R.id.barChart)
        val barData = viewModel.getBarChartData(monthlyData)

        barData?.let {
            chart.apply {
                val labels = viewModel.getLabelsForPeriod(monthlyData, selectedPeriod)

                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(labels)
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = valTextColor
                    gridColor = valGridColor
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    textSize = 10f
                }
                axisLeft.apply {
                    textColor = valTextColor
                    gridColor = valGridColor
                    setDrawGridLines(true)
                    setDrawAxisLine(false)
                    textSize = 10f
                }
                axisRight.isEnabled = false
                legend.textColor = valTextColor
                description.isEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                data = barData

                val groupSpace = 0.3f
                val barSpace = 0.05f
                val barWidth = 0.3f
                val groupWidth = (barWidth + barSpace) * 2 + groupSpace

                barData.barWidth = barWidth
                xAxis.axisMinimum = 0f
                xAxis.axisMaximum = monthlyData.size * groupWidth
                groupBars(0f, groupSpace, barSpace)

                animateY(1200, Easing.EaseInOutQuad)
                invalidate()
            }
        }
    }
}

private fun getColors(context: Context, count: Int): List<Int> {
    val all = getThemeColors(context).shuffled()
    return if (count <= all.size) all.take(count) else all
}

private fun getThemeColors(context: Context): List<Int> {
    val res = context.resources
    val packageName = context.packageName
    val colorIds = listOf(
        "ledgerly_chart1", "ledgerly_chart2", "ledgerly_chart3",
        "ledgerly_chart4", "ledgerly_chart5", "ledgerly_chart6",
        "ledgerly_chart7", "ledgerly_chart8", "ledgerly_chart9", "ledgerly_chart10"
    )

    return colorIds.map { name ->
        val id = res.getIdentifier(name, "color", packageName)
        ContextCompat.getColor(context, id)
    }
}