package ke.ac.ku.ledgerly.presentation.debt

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ke.ac.ku.ledgerly.data.constants.NavRouts
import ke.ac.ku.ledgerly.data.model.DebtEntity
import ke.ac.ku.ledgerly.presentation.settings.SettingsViewModel
import ke.ac.ku.ledgerly.ui.theme.ChartExpenseDark
import ke.ac.ku.ledgerly.ui.theme.LedgerlyGreen
import ke.ac.ku.ledgerly.ui.theme.SuccessGreenDark
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.utils.CurrencyFormatter.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.DebtListScreen(
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: DebtViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val debtListState by viewModel.debtListState.collectAsState()
    val displayCurrency by settingsViewModel.displayCurrency.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(debtListState.selectedFilter) {
        selectedTabIndex = when (debtListState.selectedFilter) {
            "overdue" -> 1
            "upcoming" -> 2
            else -> 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Debt Tracker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${debtListState.debts.size} total • ${debtListState.overdueCount} overdue",
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
                onClick = {
                    viewModel.initializeAddDebt()
                    navController.navigate(NavRouts.ADD_EDIT_DEBT)
                },
                modifier = Modifier
                    .size(56.dp)
                    .padding(bottom = 16.dp, end = 16.dp),
                containerColor = LedgerlyGreen,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add debt",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Card (Matching Savings Goal style)
            SummarySection(debtListState, displayCurrency)

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
                    "All" to debtListState.debts.size,
                    "Overdue" to debtListState.overdueCount,
                    "Upcoming" to debtListState.debts.count { it.dueDate > System.currentTimeMillis() && it.status != "settled" }
                ).forEachIndexed { index, (title, count) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            val filter = when (index) {
                                1 -> "overdue"
                                2 -> "upcoming"
                                else -> "all"
                            }
                            viewModel.setFilter(filter)
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

            if (debtListState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = LedgerlyGreen
                    )
                }
            }

            // Debts List
            if (debtListState.debts.isEmpty() && !debtListState.isLoading) {
                EmptyStateMessage()
            } else {
                val displayDebts = when (selectedTabIndex) {
                    1 -> debtListState.overdueDebts
                    2 -> debtListState.upcomingDebts
                    else -> debtListState.debts
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayDebts, key = { it.id ?: 0 }) { debt ->
                        DebtListItem(
                            debt = debt,
                            onClick = {
                                navController.navigate("${NavRouts.DEBT_DETAIL}/${debt.id}")
                            },
                            onEdit = {
                                navController.navigate("${NavRouts.ADD_EDIT_DEBT}?debtId=${debt.id}")
                            },
                            onDelete = { viewModel.deleteDebt(debt.id!!) },
                            onMarkSettled = { viewModel.markDebtAsSettled(debt.id!!) },
                            animatedVisibilityScope = animatedVisibilityScope,
                            currency = displayCurrency
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SummarySection(
    state: ke.ac.ku.ledgerly.presentation.debt.DebtListState,
    currency: String
) {
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
                    "You Owe",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatCurrency(state.totalOwe, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ChartExpenseDark
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Owed to You",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatCurrency(state.totalOwed, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreenDark
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DebtListItem(
    debt: DebtEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkSettled: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    currency: String
) {
    val isOverdue = debt.dueDate < System.currentTimeMillis() && debt.status != "settled"
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dueDate = dateFormat.format(Date(debt.dueDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key = "debt-card-${debt.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ -> tween(durationMillis = 500) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (debt.debtType == "owe") ChartExpenseDark.copy(alpha = 0.15f)
                                else SuccessGreenDark.copy(alpha = 0.15f)
                            )
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "debt-avatar-${debt.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ -> tween(durationMillis = 500) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = debt.personName.firstOrNull()?.uppercase() ?: "?",
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (debt.debtType == "owe") ChartExpenseDark else SuccessGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = debt.personName,
                            style = Typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(key = "debt-name-${debt.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                        if (debt.description.isNotEmpty()) {
                            Text(
                                text = debt.description,
                                style = Typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(debt.amount, currency),
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (debt.debtType == "owe") ChartExpenseDark else SuccessGreenDark
                    )
                    Badge(
                        containerColor = (if (debt.debtType == "owe") ChartExpenseDark else SuccessGreenDark).copy(
                            alpha = 0.15f
                        ),
                        contentColor = if (debt.debtType == "owe") ChartExpenseDark else SuccessGreenDark
                    ) {
                        Text(
                            text = if (debt.debtType == "owe") "I OWE" else "OWED",
                            style = Typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due: $dueDate",
                    style = Typography.labelSmall,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                )

                if (isOverdue) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ) {
                        Text(
                            "OVERDUE",
                            style = Typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (debt.status != "settled") {
                    Button(
                        onClick = onMarkSettled,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LedgerlyGreen.copy(
                                alpha = 0.1f
                            ), contentColor = LedgerlyGreen
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Mark Settled",
                            style = Typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateMessage() {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No debts tracked yet",
                style = Typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}