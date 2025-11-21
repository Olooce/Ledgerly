package ke.ac.ku.ledgerly.presentation.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    transactionViewModel: TransactionViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
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

    var menuExpanded by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var dateRange by remember { mutableStateOf("All Time") } // Added missing dateRange

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

                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showSearchBar = !showSearchBar },
                        colorFilter = ColorFilter.tint(iconColor)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = "Filter",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { menuExpanded = !menuExpanded },
                        colorFilter = ColorFilter.tint(iconColor)
                    )
                }
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
                        menuExpanded = menuExpanded,
                        showSearchBar = showSearchBar,
                        lazyListState = lazyListState,
                        onFilterTypeChange = {
                            transactionViewModel.updateFilter(it)
                            menuExpanded = false
                        },
                        onDateRangeChange = { dateRange = it },
                        onSearchQueryChange = { transactionViewModel.updateSearchQuery(it) },
                        onAmountRangeChange = { amountRange = it },
                        onCategoriesChange = { selectedCategories = it },
                        onLoadMore = { transactionViewModel.loadTransactions() },
                        onRefresh = { transactionViewModel.loadInitialTransactions() },
                        onClearFilters = {
                            transactionViewModel.updateFilter("All")
                            transactionViewModel.updateSearchQuery("")
                            dateRange = "All Time"
                            amountRange = null
                            selectedCategories = emptyList()
                            menuExpanded = false
                            showSearchBar = false
                        }
                    )

                    1 -> RecurringTransactionsContent(
                        recurringTransactions = filteredRecurringTransactions,
                        filterType = transactionsState.filterType,
                        statusFilter = statusFilter,
                        searchQuery = transactionsState.searchQuery,
                        amountRange = amountRange,
                        selectedCategories = selectedCategories,
                        menuExpanded = menuExpanded,
                        showSearchBar = showSearchBar,
                        onFilterTypeChange = {
                            transactionViewModel.updateFilter(it)
                            menuExpanded = false
                        },
                        onStatusFilterChange = { statusFilter = it },
                        onSearchQueryChange = { transactionViewModel.updateSearchQuery(it) },
                        onAmountRangeChange = { amountRange = it },
                        onCategoriesChange = { selectedCategories = it },
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
                            menuExpanded = false
                            showSearchBar = false
                        }
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

@Composable
fun AllTransactionsContent(
    transactions: List<TransactionEntity>,
    paginationState: PaginationState,
    filterType: String,
    dateRange: String,
    searchQuery: String,
    amountRange: ClosedFloatingPointRange<Double>?,
    selectedCategories: List<String>,
    menuExpanded: Boolean,
    showSearchBar: Boolean,
    lazyListState: LazyListState,
    onFilterTypeChange: (String) -> Unit,
    onDateRangeChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAmountRangeChange: (ClosedFloatingPointRange<Double>?) -> Unit,
    onCategoriesChange: (List<String>) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onClearFilters: () -> Unit
) {
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        AnimatedVisibility(
            visible = showSearchBar,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it })
        ) {
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
        }

        // Filter Menu
        AnimatedVisibility(
            visible = menuExpanded,
            enter = slideInVertically(initialOffsetY = { -it / 2 }),
            exit = slideOutVertically(targetOffsetY = { -it })
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Transaction Type
                    TransactionTextView(
                        "Transaction Type", style = Typography.bodySmall, color
                        = primaryTextColor, fontWeight = FontWeight.SemiBold
                    )
                    DropDown(
                        listOf("All", "Expense", "Income"),
                        onItemSelected = onFilterTypeChange
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Range
                    TransactionTextView(
                        "Date Range",
                        style = Typography.bodySmall,
                        color = primaryTextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    DropDown(
                        listOf(
                            "All Time", "Today", "Yesterday", "Last 7 Days",
                            "Last 30 Days", "Last 90 Days", "Last Year", "This Month"
                        ), onItemSelected = onDateRangeChange
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount Range
                    AmountRangeFilter(
                        amountRange = amountRange,
                        onAmountRangeChange = { onAmountRangeChange(it) })

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Filter
                    CategoryFilter(
                        availableCategories = Categories.Expenses + Categories.Income,
                        selectedCategories = selectedCategories,
                        onCategoriesChange = { onCategoriesChange(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TransactionTextView(
                        text = "Clear All Filters",
                        style = Typography.bodyMedium,
                        color = errorColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onClearFilters() }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeFilters = buildList {
                if (filterType != "All") add(filterType)
                if (dateRange != "All Time") add(dateRange)
                if (searchQuery.isNotEmpty()) add("Search")
                if (amountRange != null) add("Amount Range")
                if (selectedCategories.isNotEmpty()) add("${selectedCategories.size} Categories")
            }

            if (activeFilters.isNotEmpty()) {
                TransactionTextView(
                    text = "Filters: ${activeFilters.joinToString(", ")}",
                    style = Typography.bodySmall,
                    color = primaryTextColor,
                    modifier = Modifier.weight(1f, fill = false)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            TransactionTextView(
                text = "${transactions.size} transaction${if (transactions.size != 1) "s" else ""}",
                style = Typography.bodySmall,
                color = secondaryTextColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Show loading state for initial load
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

                // Loading indicator for pagination
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

                // Load more trigger when near the end
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

@Composable
fun RecurringTransactionsContent(
    recurringTransactions: List<RecurringTransactionEntity>,
    filterType: String,
    statusFilter: String,
    searchQuery: String,
    amountRange: ClosedFloatingPointRange<Double>?,
    selectedCategories: List<String>,
    menuExpanded: Boolean,
    showSearchBar: Boolean,
    onFilterTypeChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAmountRangeChange: (ClosedFloatingPointRange<Double>?) -> Unit,
    onCategoriesChange: (List<String>) -> Unit,
    onToggleActive: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onClearFilters: () -> Unit
) {
    val primaryTextColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        AnimatedVisibility(
            visible = showSearchBar,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it })
        ) {
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
        }

        // Filter Menu
        AnimatedVisibility(
            visible = menuExpanded,
            enter = slideInVertically(initialOffsetY = { -it / 2 }),
            exit = slideOutVertically(targetOffsetY = { -it })
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = surfaceColor,
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    TransactionTextView(
                        "Transaction Type",
                        style = Typography.bodySmall,
                        color = primaryTextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    DropDown(
                        listOf("All", "Expense", "Income"),
                        onItemSelected = onFilterTypeChange
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TransactionTextView(
                        "Status",
                        style = Typography.bodySmall,
                        color = primaryTextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    DropDown(
                        listOf("All", "Active", "Paused"),
                        onItemSelected = onStatusFilterChange
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AmountRangeFilter(
                        amountRange = amountRange,
                        onAmountRangeChange = { onAmountRangeChange(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CategoryFilter(
                        availableCategories = Categories.Expenses + Categories.Income,
                        selectedCategories = selectedCategories,
                        onCategoriesChange = { onCategoriesChange(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TransactionTextView(
                        text = "Clear All Filters",
                        style = Typography.bodyMedium,
                        color = errorColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onClearFilters() }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeFilters = buildList {
                if (filterType != "All") add(filterType)
                if (statusFilter != "All") add(statusFilter)
                if (searchQuery.isNotEmpty()) add("Search")
                if (amountRange != null) add("Amount")
                if (selectedCategories.isNotEmpty()) add("${selectedCategories.size} Categories")
            }

            if (activeFilters.isNotEmpty()) {
                TransactionTextView(
                    text = "Filters: ${activeFilters.joinToString(", ")}",
                    style = Typography.bodySmall,
                    color = primaryTextColor,
                    modifier = Modifier.weight(1f, fill = false)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            TransactionTextView(
                text = "${recurringTransactions.size} recurring",
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