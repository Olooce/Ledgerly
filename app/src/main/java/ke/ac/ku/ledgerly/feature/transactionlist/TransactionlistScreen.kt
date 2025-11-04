package ke.ac.ku.ledgerly.feature.transactionlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.feature.add_transaction.TransactionDropDown
import ke.ac.ku.ledgerly.feature.home.TransactionItem
import ke.ac.ku.ledgerly.feature.home.HomeViewModel
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
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { navController.popBackStack() },
                    colorFilter = ColorFilter.tint(Color.Black)
                )

                TransactionTextView(
                    text = "Transactions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = "Filter",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { menuExpanded = !menuExpanded },
                    colorFilter = ColorFilter.tint(Color.Black)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item {
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
                }

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
