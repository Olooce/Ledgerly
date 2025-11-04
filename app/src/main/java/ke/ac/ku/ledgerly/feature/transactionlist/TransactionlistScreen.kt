package ke.ac.ku.ledgerly.feature.transactionlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
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
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.feature.add_transaction.TransactionDropDown
import ke.ac.ku.ledgerly.feature.home.HomeViewModel
import ke.ac.ku.ledgerly.feature.home.TransactionItem
import ke.ac.ku.ledgerly.ui.theme.DeepNavy
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.ui.theme.White
import ke.ac.ku.ledgerly.ui.theme.Zinc
import ke.ac.ku.ledgerly.utils.Utils
import ke.ac.ku.ledgerly.widget.TransactionTextView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionListScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState(initial = emptyList<TransactionEntity>())
    var filterType by remember { mutableStateOf("All") }
    var dateRange by remember { mutableStateOf("All Time") }
    var menuExpanded by remember { mutableStateOf(false) }

    // Filter by type
    val filteredByType = when (filterType) {
        "Expense" -> transactions.filter { it.type.equals("Expense", true) }
        "Income" -> transactions.filter { it.type.equals("Income", true) }
        else -> transactions
    }

    // TODO: Add actual date filtering logic
    val filteredTransactions = filteredByType.filter { _ -> true }

    Scaffold(
        floatingActionButton = {
            var expanded by remember { mutableStateOf(false) }

            Box(contentAlignment = Alignment.BottomEnd) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedVisibility(visible = expanded) {
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(16.dp)) {
                            // Add Income
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color = Zinc, shape = RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.onEvent(ke.ac.ku.ledgerly.feature.home.HomeUiEvent.OnAddIncomeClicked)
                                        expanded = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_income),
                                    contentDescription = "Add Income",
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Add Expense
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color = Zinc, shape = RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.onEvent(ke.ac.ku.ledgerly.feature.home.HomeUiEvent.OnAddExpenseClicked)
                                        expanded = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_expense),
                                    contentDescription = "Add Expense",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Main FAB
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(60.dp)
//                            .clip(RoundedCornerShape(16.dp))
                            .background(color = Zinc)
                            .clickable {
                                expanded = !expanded
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_addbutton),
                            contentDescription = "Add Transaction",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(padding)
        ) {
            val (topBar, header, content) = createRefs()

            // Top
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
                    TransactionTextView(
                        text = dateRange,
                        style = Typography.bodyMedium,
                        color = DeepNavy
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = "Filter",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { menuExpanded = !menuExpanded },
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            // Main content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .constrainAs(content) {
                        top.linkTo(header.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    }
            ) {
                // Filter dropdown menu
                AnimatedVisibility(
                    visible = menuExpanded,
                    enter = slideInVertically(initialOffsetY = { -it / 2 }),
                    exit = slideOutVertically(targetOffsetY = { -it }),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Column {
                        TransactionDropDown(
                            listOfItems = listOf("All", "Expense", "Income"),
                            onItemSelected = { selected ->
                                filterType = selected
                                menuExpanded = false
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TransactionDropDown(
                            listOfItems = listOf(
                                "All Time",
                                "Today",
                                "Yesterday",
                                "Last 30 Days",
                                "Last 90 Days",
                                "Last Year"
                            ),
                            onItemSelected = { selected ->
                                dateRange = selected
                                menuExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Transaction list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTransactions) { transaction ->
                        val icon = Utils.getItemIcon(transaction.category)
                        TransactionItem(
                            title = transaction.category,
                            paymentMethod = transaction.paymentMethod,
                            amount = Utils.formatCurrency(transaction.amount),
                            icon = icon ?: R.drawable.ic_default_category,
                            date = transaction.date,
                            notes = transaction.notes,
                            tags = transaction.tags,
                            color = if (transaction.type.equals("Income", true)) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.animateItemPlacement(tween(100))
                        )
                    }
                }
            }
        }
    }
}