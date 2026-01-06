package ke.ac.ku.ledgerly.presentation.bill_reminders

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                BillReminderNavigationEvent.NavigateBack -> navController.popBackStack()
                is BillReminderNavigationEvent.NavigateToAddEditBill -> {
                    navController.navigate(
                        "${NavRouts.ADD_EDIT_BILL_REMINDER}?billId=${event.billId}"
                    )

                }
//                is BillReminderNavigationEvent.NavigateToBillDetail -> {
//                    navController.navigate("bill_detail/${event.billId}")
//                }
                else -> {}
            }
        }
    }

    val filteredBills = when (listState.selectedFilter) {
        "upcoming" -> upcomingBills
        "overdue" -> overdueBills
        else -> allBills
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Bill Reminders",
                            style = Typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.onAddNewBill() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Bill")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Summary Cards
                item {
                    SummarySection(listState.summary)
                }

                // Filter Tabs
                item {
                    BillReminderFilterTabs(
                        selectedFilter = listState.selectedFilter,
                        onFilterSelected = { viewModel.setFilter(it) }
                    )
                }

                // Bills List
                if (filteredBills.isEmpty()) {
                    item {
                        EmptyStateMessage(listState.selectedFilter)
                    }
                } else {
                    items(filteredBills) { bill ->
                        BillReminderCard(
                            bill = bill,
                            onEdit = { viewModel.onEditBill(bill.id ?: return@BillReminderCard) },
                            onDelete = { viewModel.onDeleteBill(bill.id ?: return@BillReminderCard) },
                            onMarkAsPaid = { viewModel.onMarkAsPaid(bill.id ?: return@BillReminderCard) },
                            onClick = { viewModel.onBillReminderClick(bill.id ?: return@BillReminderCard) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SummarySection(summary: BillReminderSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Upcoming",
                value = summary.totalUpcoming.toString(),
                subtitle = "KES ${String.format("%.0f", summary.totalAmount)}",
                backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.12f),
                borderColor = Color(0xFF4CAF50).copy(alpha = 0.3f),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Overdue",
                value = summary.overdueCount.toString(),
                subtitle = "Needs attention",
                backgroundColor = Color(0xFFEF4444).copy(alpha = 0.12f),
                borderColor = Color(0xFFEF4444).copy(alpha = 0.3f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    backgroundColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
            )
        }
    }
}

@Composable
private fun BillReminderFilterTabs(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    TabRow(
        selectedTabIndex = when (selectedFilter) {
            "upcoming" -> 1
            "overdue" -> 2
            else -> 0
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        containerColor = MaterialTheme.colorScheme.background,
        indicator = {}
    ) {
        Tab(
            selected = selectedFilter == "all",
            onClick = { onFilterSelected("all") },
            text = { Text("All", style = Typography.labelMedium) }
        )
        Tab(
            selected = selectedFilter == "upcoming",
            onClick = { onFilterSelected("upcoming") },
            text = { Text("Upcoming", style = Typography.labelMedium) }
        )
        Tab(
            selected = selectedFilter == "overdue",
            onClick = { onFilterSelected("overdue") },
            text = { Text("Overdue", style = Typography.labelMedium) }
        )
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
        bill.isOverdue -> Color(0xFFEF4444) to "OVERDUE"
        bill.daysUntilDue == 0 -> Color(0xFFFFA500) to "DUE TODAY"
        bill.daysUntilDue > 0 && bill.daysUntilDue <= 3 -> Color(0xFFFFB74D) to "SOON"
        else -> Color(0xFF4CAF50) to "UPCOMING"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2C).copy(alpha = 0.8f)
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.2f),
                    Color.White.copy(alpha = 0.05f)
                ),
                start = Offset(0f, 0f),
                end = Offset(500f, 500f)
            )
        )
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

                    if (bill.description.isNotEmpty()) {
                        Text(
                            text = bill.description,
                            style = Typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

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
                            .background(statusColor.copy(alpha = 0.2f))
                            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
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

            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (bill.status == "pending") {
                        IconButton(
                            onClick = onMarkAsPaid,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = "Mark as Paid",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFEF4444)
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
            .fillMaxWidth()
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

            Text(
                text = "Create your first bill reminder",
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}