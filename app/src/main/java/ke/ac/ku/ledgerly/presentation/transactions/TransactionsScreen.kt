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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ke.ac.ku.ledgerly.data.constants.NavRouts
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.presentation.home.HomeUiEvent
import ke.ac.ku.ledgerly.presentation.home.HomeViewModel
import ke.ac.ku.ledgerly.ui.theme.DeepNavy
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.ui.widget.DropDown
import ke.ac.ku.ledgerly.ui.widget.MultiFloatingActionButton
import ke.ac.ku.ledgerly.ui.widget.RecurringTransactionItem
import ke.ac.ku.ledgerly.ui.widget.TransactionItem
import ke.ac.ku.ledgerly.ui.widget.TransactionTextView
import ke.ac.ku.ledgerly.utils.FormatingUtils
import ke.ac.ku.ledgerly.utils.Utils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    recurringViewModel: TransactionViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All Transactions", "Recurring")

    val transactions by homeViewModel.transactions.collectAsState(initial = emptyList())
    val recurringTransactions by recurringViewModel.recurringTransactions.collectAsState(initial = emptyList())

    var filterType by remember { mutableStateOf("All") }
    var dateRange by remember { mutableStateOf("All Time") }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        homeViewModel.navigationEvent.collect { event ->
            when (event) {
                HomeNavigationEvent.NavigateToAddIncome -> navController.navigate(NavRouts.addIncome)
                HomeNavigationEvent.NavigateToAddExpense -> navController.navigate(NavRouts.addExpense)
                else -> {}
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
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
                        .clickable { navController.popBackStack() },
                    colorFilter = ColorFilter.tint(Color.White)
                )

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TransactionTextView(
                        text = "Transactions",
                        style = Typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedTab == 0) {
                        TransactionTextView(
                            text = dateRange,
                            style = Typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }

                if (selectedTab == 0) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = "Filter",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { menuExpanded = !menuExpanded },
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }

            // Tab Row
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
                contentColor = DeepNavy
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
                                color = if (selectedTab == index) DeepNavy else Color.Gray
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
                        transactions = transactions,
                        filterType = filterType,
                        dateRange = dateRange,
                        menuExpanded = menuExpanded,
                        onFilterTypeChange = { filterType = it; menuExpanded = false },
                        onDateRangeChange = { dateRange = it; menuExpanded = false }
                    )
                    1 -> RecurringTransactionsContent(
                        recurringTransactions = recurringTransactions,
                        onToggleActive = { id, isActive ->
                            recurringViewModel.toggleRecurringTransactionStatus(id, isActive)
                        },
                        onDelete = { id ->
                            recurringViewModel.deleteRecurringTransaction(id)
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
    filterType: String,
    dateRange: String,
    menuExpanded: Boolean,
    onFilterTypeChange: (String) -> Unit,
    onDateRangeChange: (String) -> Unit
) {
    val filteredByType = when (filterType) {
        "Expense" -> transactions.filter { it.type.equals("Expense", true) }
        "Income" -> transactions.filter { it.type.equals("Income", true) }
        else -> transactions
    }

    Column {
        AnimatedVisibility(
            visible = menuExpanded,
            enter = slideInVertically(initialOffsetY = { -it / 2 }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.padding(8.dp)
        ) {
            Column {
                DropDown(
                    listOfItems = listOf("All", "Expense", "Income"),
                    onItemSelected = onFilterTypeChange
                )

                Spacer(modifier = Modifier.height(8.dp))

                DropDown(
                    listOfItems = listOf(
                        "All Time", "Today", "Yesterday",
                        "Last 30 Days", "Last 90 Days", "Last Year"
                    ),
                    onItemSelected = onDateRangeChange
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredByType) { transaction ->
                val trans = transaction
                TransactionItem(
                    title = trans.category,
                    paymentMethod = trans.paymentMethod,
                    amount = FormatingUtils.formatCurrency(trans.amount),
                    date = FormatingUtils.formatDateToHumanReadableForm(trans.date),
                    notes = trans.notes,
                    tags = trans.tags,
                    color = if (trans.type.equals("Income", true))
                        Color(0xFF2E7D32) else Color(0xFFC62828),
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
fun RecurringTransactionsContent(
    recurringTransactions: List<RecurringTransactionEntity>,
    onToggleActive: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    if (recurringTransactions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TransactionTextView(
                text = "No recurring transactions yet",
                style = Typography.bodyLarge,
                color = DeepNavy
            )
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



