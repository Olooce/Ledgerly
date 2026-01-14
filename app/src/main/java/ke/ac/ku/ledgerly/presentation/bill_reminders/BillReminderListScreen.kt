package ke.ac.ku.ledgerly.presentation.bill_reminders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.base.BillReminderNavigationEvent
import ke.ac.ku.ledgerly.data.constants.NavRouts
import ke.ac.ku.ledgerly.data.model.BillReminderEntity
import ke.ac.ku.ledgerly.data.model.BillReminderSummary
import ke.ac.ku.ledgerly.ui.theme.LedgerlyGreen
import ke.ac.ku.ledgerly.ui.theme.StatusDueToday
import ke.ac.ku.ledgerly.ui.theme.StatusOverdue
import ke.ac.ku.ledgerly.ui.theme.StatusSoon
import ke.ac.ku.ledgerly.ui.theme.StatusUpcoming
import ke.ac.ku.ledgerly.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillReminderListScreen(
    navController: NavController,
    viewModel: BillReminderViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val allBills by viewModel.allBills.collectAsState()
    val upcomingBills by viewModel.upcomingBills.collectAsState()
    val overdueBills by viewModel.overdueBills.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                BillReminderNavigationEvent.NavigateBack -> navController.popBackStack()
                is BillReminderNavigationEvent.NavigateToAddEditBill -> {
                    val route = if (event.billId != null) {
                        "${NavRouts.ADD_EDIT_BILL_REMINDER}?billId=${event.billId}"
                    } else {
                        NavRouts.ADD_EDIT_BILL_REMINDER
                    }
                    navController.navigate(route)
                }

                else -> {}
            }
        }
    }

    LaunchedEffect(listState.selectedFilter) {
        selectedTabIndex = when (listState.selectedFilter) {
            "upcoming" -> 1
            "overdue" -> 2
            else -> 0
        }
    }

    val filteredBills = when (selectedTabIndex) {
        1 -> upcomingBills
        2 -> overdueBills
        else -> allBills
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Bill Reminders",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${upcomingBills.size} upcoming • ${overdueBills.size} overdue",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddNewBill() },
                containerColor = LedgerlyGreen,
                contentColor = Color.White,
                modifier = Modifier
                    .size(56.dp)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Bill")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Card (Matching Savings Goal style)
            SummarySection(listState.summary)

            // Tabs (Matching Savings Goal style)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 3.dp,
                        color = LedgerlyGreen
                    )
                }
            ) {
                listOf(
                    "All" to allBills.size,
                    "Upcoming" to upcomingBills.size,
                    "Overdue" to overdueBills.size
                ).forEachIndexed { index, (title, count) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            viewModel.setFilter(
                                when (index) {
                                    1 -> "upcoming"
                                    2 -> "overdue"
                                    else -> "all"
                                }
                            )
                        }
                    ) {
                        Text(
                            "$title ($count)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) LedgerlyGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }

            if (filteredBills.isEmpty()) {
                EmptyStateMessage(listState.selectedFilter)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBills) { bill ->
                        BillReminderCard(
                            bill = bill,
                            onEdit = { viewModel.onEditBill(bill.id ?: return@BillReminderCard) },
                            onDelete = {
                                viewModel.onDeleteBill(
                                    bill.id ?: return@BillReminderCard
                                )
                            },
                            onMarkAsPaid = {
                                viewModel.onMarkAsPaid(
                                    bill.id ?: return@BillReminderCard
                                )
                            },
                            onClick = {
                                viewModel.onBillReminderClick(
                                    bill.id ?: return@BillReminderCard
                                )
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SummarySection(summary: BillReminderSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LedgerlyGreen.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Upcoming Bills",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${summary.totalUpcoming} bills",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LedgerlyGreen
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Total Amount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "KES ${String.format("%.0f", summary.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LedgerlyGreen
                )
            }
        }
    }
}

@Composable
private fun BillReminderCard(
    bill: BillReminderEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkAsPaid: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dueDate = dateFormat.format(Date(bill.dueDate))

    val (statusColor, statusText) = when {
        bill.isOverdue -> StatusOverdue to "OVERDUE"
        bill.daysUntilDue == 0 -> StatusDueToday to "DUE TODAY"
        bill.daysUntilDue > 0 && bill.daysUntilDue <= 3 -> StatusSoon to "SOON"
        else -> StatusUpcoming to "UPCOMING"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = bill.billName,
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bill.category,
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "KES ${String.format("%.2f", bill.amount)}",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(bill.color)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = Typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due: $dueDate",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (bill.status == "pending") {
                        IconButton(onClick = onMarkAsPaid, modifier = Modifier.size(28.dp)) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = "Mark as Paid",
                                modifier = Modifier.size(16.dp),
                                tint = StatusUpcoming
                            )
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp),
                            tint = StatusOverdue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(filter: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_empty_state),
                contentDescription = "No Bills",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = when (filter) {
                    "upcoming" -> "No upcoming bills"
                    "overdue" -> "No overdue bills"
                    else -> "No bills yet"
                },
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
