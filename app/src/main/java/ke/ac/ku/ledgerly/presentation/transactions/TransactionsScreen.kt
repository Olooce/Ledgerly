package ke.ac.ku.ledgerly.presentation.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.base.HomeNavigationEvent
import ke.ac.ku.ledgerly.data.constants.Categories
import ke.ac.ku.ledgerly.data.constants.NavRouts
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.presentation.home.HomeUiEvent
import ke.ac.ku.ledgerly.presentation.home.HomeViewModel
import ke.ac.ku.ledgerly.ui.components.RecurringTransactionItem
import ke.ac.ku.ledgerly.ui.components.TransactionItem
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.ui.widget.AmountRangeFilter
import ke.ac.ku.ledgerly.ui.widget.CategoryFilter
import ke.ac.ku.ledgerly.ui.widget.DropDown
import ke.ac.ku.ledgerly.ui.widget.MultiFloatingActionButton
import ke.ac.ku.ledgerly.ui.widget.SearchBar
import ke.ac.ku.ledgerly.ui.widget.TransactionTextView
import ke.ac.ku.ledgerly.utils.FormatingUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    transactionViewModel: TransactionViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All Transactions", "Recurring")

    val transactionsState by transactionViewModel.transactionsState.collectAsState()
    val recurringTransactions by transactionViewModel.recurringTransactionsState.collectAsState()

    val lazyListState = rememberLazyListState()
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layoutInfo ->
                val shouldLoadMore = layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { lastVisibleIndex ->
                    lastVisibleIndex >= layoutInfo.totalItemsCount - 5
                } ?: false

                if (shouldLoadMore && transactionsState.paginationState.hasNext && !transactionsState.paginationState.isLoading) {
                    transactionViewModel.loadTransactions()
                }
            }
    }

    // Clear transactions when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            transactionViewModel.clearTransactions()
        }
    }

    var filtersExpanded by remember { mutableStateOf(false) }
    val dateRange = transactionsState.dateRange

    val iconColor = Color.White
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    var amountRange by remember { mutableStateOf<ClosedFloatingPointRange<Double>?>(null) }
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var statusFilter by remember { mutableStateOf("All") }

    val displayedTransactions = transactionsState.transactions

    val filteredRecurringTransactions = remember(
        recurringTransactions,
        transactionsState.filterType,
        transactionsState.searchQuery,
        amountRange,
        selectedCategories,
        statusFilter
    ) {
        var filtered = recurringTransactions

        if (transactionsState.filterType != "All") {
            filtered = filtered.filter {
                it.type.equals(transactionsState.filterType, ignoreCase = true)
            }
        }

        if (transactionsState.searchQuery.isNotEmpty()) {
            filtered = filtered.filter { recurring ->
                recurring.category.contains(transactionsState.searchQuery, ignoreCase = true) ||
                        recurring.notes.contains(transactionsState.searchQuery, ignoreCase = true)
            }
        }

        amountRange?.let { range ->
            filtered = filtered.filter {
                it.amount in range.start..range.endInclusive
            }
        }

        if (selectedCategories.isNotEmpty()) {
            filtered = filtered.filter {
                selectedCategories.contains(it.category)
            }
        }

        when (statusFilter) {
            "Active" -> filtered = filtered.filter { it.isActive }
            "Paused" -> filtered = filtered.filter { !it.isActive }
        }

        filtered
    }

    LaunchedEffect(Unit) {
        homeViewModel.navigationEvent.collect { event ->
            when (event) {
                HomeNavigationEvent.NavigateToAddIncome -> navController.navigate(NavRouts.addIncome)
                HomeNavigationEvent.NavigateToAddExpense -> navController.navigate(NavRouts.addExpense)
                else -> {}
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (topBar, header, tabRow, content, add) = createRefs()

            // Top Bar
            Image(
                painter = painterResource(id = R.drawable.ic_topbar),
                contentDescription = null,
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                    .constrainAs(header) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(24.dp)
                        .clickable { navController.popBackStack() },
                    colorFilter = ColorFilter.tint(iconColor)
                )

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TransactionTextView(
                        text = "Transactions",
                        style = Typography.titleLarge,
                        color = iconColor,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedTab == 0) {
                        TransactionTextView(
                            text = dateRange,
                            style = Typography.bodyMedium,
                            color = iconColor.copy(alpha = 0.9f)
                        )
                    } else {
                        TransactionTextView(
                            text = statusFilter,
                            style = Typography.bodyMedium,
                            color = iconColor.copy(alpha = 0.9f)
                        )
                    }
                }

//                Row(
//                    modifier = Modifier.align(Alignment.CenterEnd),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_filter),
//                        contentDescription = "Filter",
//                        modifier = Modifier
//                            .size(24.dp)
//                            .clickable { filtersExpanded = !filtersExpanded },
//                        colorFilter = ColorFilter.tint(iconColor)
//                    )
//                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(tabRow) {
                        top.linkTo(header.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                containerColor = Color.Transparent,
                contentColor = primaryTextColor
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            TransactionTextView(
                                text = title,
                                style = Typography.bodyLarge,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) primaryTextColor else secondaryTextColor
                            )
                        }
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .constrainAs(content) {
                        top.linkTo(tabRow.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    }
            ) {
                when (selectedTab) {
                    0 -> AllTransactionsContent(
                        transactions = displayedTransactions,
                        paginationState = transactionsState.paginationState,
                        filterType = transactionsState.filterType,
                        dateRange = dateRange,
                        searchQuery = transactionsState.searchQuery,
                        amountRange = amountRange,
                        selectedCategories = selectedCategories,
                        filtersExpanded = filtersExpanded,
                        lazyListState = lazyListState,
                        onFilterTypeChange = {
                            transactionViewModel.updateFilter(it)
                        },
                        onDateRangeChange = {
                            transactionViewModel.updateDateRange(it)
                        },
                        onSearchQueryChange = { transactionViewModel.updateSearchQuery(it) },
                        onAmountRangeChange = { amountRange = it },
                        onCategoriesChange = { selectedCategories = it },
                        onLoadMore = { transactionViewModel.loadTransactions() },
                        onRefresh = { transactionViewModel.loadInitialTransactions() },
                        onClearFilters = {
                            transactionViewModel.updateFilter("All")
                            transactionViewModel.updateSearchQuery("")
                            transactionViewModel.updateDateRange("All Time")
                            amountRange = null
                            selectedCategories = emptyList()
                        },
                        onToggleFilters = { filtersExpanded = !filtersExpanded }
                    )

                    1 -> RecurringTransactionsContent(
                        recurringTransactions = filteredRecurringTransactions,
                        filterType = transactionsState.filterType,
                        statusFilter = statusFilter,
                        searchQuery = transactionsState.searchQuery,
                        amountRange = amountRange,
                        selectedCategories = selectedCategories,
                        filtersExpanded = filtersExpanded,
                        onFilterTypeChange = {
                            transactionViewModel.updateFilter(it)
                        },
                        onSearchQueryChange = { transactionViewModel.updateSearchQuery(it) },
                        onAmountRangeChange = { amountRange = it },
                        onCategoriesChange = { selectedCategories = it },
                        onStatusFilterChange = { statusFilter = it },
                        onToggleActive = { id, isActive ->
                            transactionViewModel.toggleRecurringTransactionStatus(id, isActive)
                        },
                        onDelete = { id ->
                            transactionViewModel.deleteRecurringTransaction(id)
                        },
                        onClearFilters = {
                            transactionViewModel.updateFilter("All")
                            transactionViewModel.updateSearchQuery("")
                            statusFilter = "All"
                            amountRange = null
                            selectedCategories = emptyList()
                        },
                        onToggleFilters = { filtersExpanded = !filtersExpanded }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(add) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    },
                contentAlignment = Alignment.BottomEnd
            ) {
                MultiFloatingActionButton(
                    modifier = Modifier,
                    onAddExpenseClicked = {
                        homeViewModel.onEvent(HomeUiEvent.OnAddExpenseClicked)
                    },
                    onAddIncomeClicked = {
                        homeViewModel.onEvent(HomeUiEvent.OnAddIncomeClicked)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsContent(
    transactions: List<TransactionEntity>,
    paginationState: PaginationState,
    filterType: String,
    dateRange: String,
    searchQuery: String,
    amountRange: ClosedFloatingPointRange<Double>?,
    selectedCategories: List<String>,
    filtersExpanded: Boolean,
    lazyListState: LazyListState,
    onFilterTypeChange: (String) -> Unit,
    onDateRangeChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAmountRangeChange: (ClosedFloatingPointRange<Double>?) -> Unit,
    onCategoriesChange: (List<String>) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onClearFilters: () -> Unit,
    onToggleFilters: () -> Unit
) {
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(searchQuery) {
        delay(300)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onCloseSearch = {
                onSearchQueryChange("")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        ActiveFiltersChips(
            filterType = filterType,
            dateRange = dateRange,
            amountRange = amountRange,
            selectedCategories = selectedCategories,
            searchQuery = searchQuery,
            onFilterTypeChange = onFilterTypeChange,
            onDateRangeChange = onDateRangeChange,
            onAmountRangeChange = onAmountRangeChange,
            onCategoriesChange = onCategoriesChange,
            onSearchQueryChange = onSearchQueryChange,
            onClearAll = onClearFilters
        )
        FilterPanel(
            isExpanded = filtersExpanded,
            onToggle = onToggleFilters,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceVariant),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    TransactionTextView(
                        text = "Quick Filters",
                        style = Typography.bodySmall,
                        color = primaryTextColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Transaction Type
                        Column(modifier = Modifier.weight(1f)) {
                            TransactionTextView(
                                "Type",
                                style = Typography.bodySmall,
                                color = primaryTextColor.copy(alpha = 0.8f)
                            )
                            DropDown(
                                listOf("All", "Expense", "Income"),
                                onItemSelected = onFilterTypeChange,
                            )
                        }

                        // Date Range
                        Column(modifier = Modifier.weight(1f)) {
                            TransactionTextView(
                                "Date Range",
                                style = Typography.bodySmall,
                                color = primaryTextColor.copy(alpha = 0.8f)
                            )
                            DropDown(
                                listOf(
                                    "All Time", "Today", "Yesterday", "Last 7 Days",
                                    "Last 30 Days", "Last 90 Days", "Last Year", "This Month"
                                ),
                                onItemSelected = onDateRangeChange,
                            )
                        }
                    }
                }
            }

            // Expandable amount filter
            ExpandableFilterSection(
                title = "Amount Range",
                isExpandedByDefault = amountRange != null
            ) {
                AmountRangeFilter(
                    amountRange = amountRange,
                    onAmountRangeChange = onAmountRangeChange
                )
            }

            // Expandable category filter
            ExpandableFilterSection(
                title = "Categories",
                isExpandedByDefault = selectedCategories.isNotEmpty()
            ) {
                CategoryFilter(
                    availableCategories = Categories.Expenses + Categories.Income,
                    selectedCategories = selectedCategories,
                    onCategoriesChange = onCategoriesChange
                )
            }

            // Clear filters button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TransactionTextView(
                    text = "Clear All",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onClearFilters() }
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transaction count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransactionTextView(
                text = "${transactions.size} transaction${if (transactions.size != 1) "s" else ""}",
                style = Typography.bodySmall,
                color = secondaryTextColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transaction list
        if (transactions.isEmpty() && paginationState.isLoading && paginationState.currentPage == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_empty_state),
                        contentDescription = "No transactions",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(bottom = 16.dp),
                        colorFilter = ColorFilter.tint(secondaryTextColor.copy(alpha = 0.5f))
                    )
                    TransactionTextView(
                        text = if (filterType != "All" || dateRange != "All Time" || searchQuery.isNotEmpty() || amountRange != null || selectedCategories.isNotEmpty()) {
                            "No transactions match your filters"
                        } else {
                            "No transactions yet"
                        },
                        style = Typography.bodyLarge,
                        color = secondaryTextColor
                    )
                }
            }
        } else {
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(transactions) { transaction ->
                    TransactionItem(
                        title = transaction.category,
                        paymentMethod = transaction.paymentMethod,
                        amount = FormatingUtils.formatCurrency(transaction.amount),
                        date = FormatingUtils.formatDateToHumanReadableForm(transaction.date),
                        notes = transaction.notes,
                        tags = transaction.tags,
                        color = if (transaction.type.equals("Income", true))
                            Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier
                    )
                }

                if (paginationState.isLoading && paginationState.currentPage > 0) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                if (paginationState.hasNext && !paginationState.isLoading) {
                    item {
                        LaunchedEffect(Unit) {
                            onLoadMore()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionsContent(
    recurringTransactions: List<RecurringTransactionEntity>,
    filterType: String,
    statusFilter: String,
    searchQuery: String,
    amountRange: ClosedFloatingPointRange<Double>?,
    selectedCategories: List<String>,
    filtersExpanded: Boolean,
    onFilterTypeChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAmountRangeChange: (ClosedFloatingPointRange<Double>?) -> Unit,
    onCategoriesChange: (List<String>) -> Unit,
    onToggleActive: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onClearFilters: () -> Unit,
    onToggleFilters: () -> Unit
) {
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(searchQuery) {
        delay(300)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Always visible search bar
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onCloseSearch = {
                onSearchQueryChange("")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Active filter chips
        ActiveFiltersChips(
            filterType = filterType,
            dateRange = "All Time",
            amountRange = amountRange,
            selectedCategories = selectedCategories,
            searchQuery = searchQuery,
            statusFilter = statusFilter,
            onFilterTypeChange = onFilterTypeChange,
            onDateRangeChange = {  },
            onAmountRangeChange = onAmountRangeChange,
            onCategoriesChange = onCategoriesChange,
            onSearchQueryChange = onSearchQueryChange,
            onStatusFilterChange = onStatusFilterChange,
            onClearAll = onClearFilters
        )

        // Persistent filter panel
        FilterPanel(
            isExpanded = filtersExpanded,
            onToggle = onToggleFilters,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            // Combined main filters row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceVariant),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    TransactionTextView(
                        text = "Quick Filters",
                        style = Typography.bodySmall,
                        color = primaryTextColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Transaction Type
                        Column(modifier = Modifier.weight(1f)) {
                            TransactionTextView(
                                "Type",
                                style = Typography.bodySmall,
                                color = primaryTextColor.copy(alpha = 0.8f)
                            )
                            DropDown(
                                listOf("All", "Expense", "Income"),
                                onItemSelected = onFilterTypeChange,
                            )
                        }

                        // Status Filter
                        Column(modifier = Modifier.weight(1f)) {
                            TransactionTextView(
                                "Status",
                                style = Typography.bodySmall,
                                color = primaryTextColor.copy(alpha = 0.8f)
                            )
                            DropDown(
                                listOf("All", "Active", "Paused"),
                                onItemSelected = onStatusFilterChange,
                            )
                        }
                    }
                }
            }

            // Expandable amount filter
            ExpandableFilterSection(
                title = "Amount Range",
                isExpandedByDefault = amountRange != null
            ) {
                AmountRangeFilter(
                    amountRange = amountRange,
                    onAmountRangeChange = onAmountRangeChange
                )
            }

            // Expandable category filter
            ExpandableFilterSection(
                title = "Categories",
                isExpandedByDefault = selectedCategories.isNotEmpty()
            ) {
                CategoryFilter(
                    availableCategories = Categories.Expenses + Categories.Income,
                    selectedCategories = selectedCategories,
                    onCategoriesChange = onCategoriesChange
                )
            }

            // Clear filters button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TransactionTextView(
                    text = "Clear All",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onClearFilters() }
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transaction count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransactionTextView(
                text = "${recurringTransactions.size} recurring transaction${if (recurringTransactions.size != 1) "s" else ""}",
                style = Typography.bodySmall,
                color = secondaryTextColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (recurringTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_empty_state),
                        contentDescription = "No recurring transactions",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(bottom = 16.dp),
                        colorFilter = ColorFilter.tint(secondaryTextColor.copy(alpha = 0.5f))
                    )
                    TransactionTextView(
                        text = if (filterType != "All" || statusFilter != "All" || searchQuery.isNotEmpty() || amountRange != null || selectedCategories.isNotEmpty()) {
                            "No recurring transactions match your filters"
                        } else {
                            "No recurring transactions yet"
                        },
                        style = Typography.bodyLarge,
                        color = secondaryTextColor
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(recurringTransactions) { recurring ->
                    RecurringTransactionItem(
                        recurring = recurring,
                        onToggleActive = onToggleActive,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveFiltersChips(
    filterType: String,
    dateRange: String,
    amountRange: ClosedFloatingPointRange<Double>?,
    selectedCategories: List<String>,
    searchQuery: String,
    statusFilter: String? = null,
    onFilterTypeChange: (String) -> Unit,
    onDateRangeChange: (String) -> Unit,
    onAmountRangeChange: (ClosedFloatingPointRange<Double>?) -> Unit,
    onCategoriesChange: (List<String>) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: ((String) -> Unit)? = null,
    onClearAll: () -> Unit
) {
    val activeFilters = buildList {
        if (filterType != "All") add(FilterChipData("Type: $filterType") { onFilterTypeChange("All") })
        if (dateRange != "All Time") add(FilterChipData("Date: $dateRange") { onDateRangeChange("All Time") })
        if (amountRange != null) add(FilterChipData(
            "Amount: ${FormatingUtils.formatCurrency(amountRange.start)} - ${FormatingUtils.formatCurrency(amountRange.endInclusive)}"
        ) { onAmountRangeChange(null) })
        if (selectedCategories.isNotEmpty()) {
            selectedCategories.forEach { category ->
                add(FilterChipData(category) {
                    onCategoriesChange(selectedCategories - category)
                })
            }
        }
        if (searchQuery.isNotEmpty()) add(FilterChipData("Search: $searchQuery") { onSearchQueryChange("") })
        if (statusFilter != null && statusFilter != "All") {
            add(FilterChipData("Status: $statusFilter") { onStatusFilterChange?.invoke("All") })
        }
    }

    if (activeFilters.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Active Filters:",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                // Clear all button
                Text(
                    text = "Clear all",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onClearAll() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeFilters.chunked(2)) { rowFilters ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowFilters.forEach { filter ->
                            FilterChip(
                                text = filter.text,
                                onRemove = filter.onRemove
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                shape = CircleShape
            ),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = Typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove filter",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() },
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun FilterPanel(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Filter header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = Typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse filters" else "Expand filters",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Filter content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ExpandableFilterSection(
    title: String,
    isExpandedByDefault: Boolean = false,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(isExpandedByDefault) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = Typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse $title" else "Expand $title",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

data class FilterChipData(
    val text: String,
    val onRemove: () -> Unit
)