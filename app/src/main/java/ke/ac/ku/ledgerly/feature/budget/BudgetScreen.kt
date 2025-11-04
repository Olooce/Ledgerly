package ke.ac.ku.ledgerly.feature.budget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.feature.add_transaction.AddTransactionViewModel
import ke.ac.ku.ledgerly.ui.theme.*
import ke.ac.ku.ledgerly.utils.Utils
import ke.ac.ku.ledgerly.widget.TransactionTextView

@Composable
fun BudgetScreen(
    navController: NavController,
    viewModel: BudgetViewModel = hiltViewModel(),
    addTransactionViewModel: AddTransactionViewModel = hiltViewModel()
) {
    val budgets by viewModel.budgets.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Initial load
    LaunchedEffect(Unit) {
        viewModel.loadBudgets()
        viewModel.loadAlerts()
    }

    // Reactive reload when a new transaction is added
    LaunchedEffect(Unit) {
        addTransactionViewModel.transactionAdded.collect {
            viewModel.loadBudgets()
            viewModel.loadAlerts()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_budget") },
                containerColor = Zinc,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
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

            // Top bar image
            Image(
                painter = painterResource(id = R.drawable.ic_topbar),
                contentDescription = null,
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            // Header section
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
                Column {
                    TransactionTextView(
                        text = "Budgets Overview",
                        style = Typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    TransactionTextView(
                        text = Utils.formatMonthYear(Utils.getCurrentMonthYear()),
                        style = Typography.bodyMedium,
                        color = DeepNavy
                    )
                }
//                Image(
//                    painter = painterResource(id = R.drawable.ic_notification),
//                    contentDescription = null,
//                    modifier = Modifier.align(Alignment.CenterEnd)
//                )
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
                if (alerts.isNotEmpty()) {
                    AlertSection(alerts = alerts)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                when (uiState) {
                    is BudgetUiState.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = Green) }

                    is BudgetUiState.Error -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        TransactionTextView(
                            text = (uiState as BudgetUiState.Error).message,
                            color = Red
                        )
                    }

                    else -> {
                        if (budgets.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                TransactionTextView(
                                    text = "No budgets set for ${Utils.formatMonthYear(Utils.getCurrentMonthYear())}",
                                    color = LightGrey
                                )
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(budgets) { budget ->
                                    BudgetItem(
                                        budget = budget,
                                        onDelete = { viewModel.deleteBudget(budget.category) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertSection(alerts: List<BudgetEntity>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Red.copy(alpha = 0.15f))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Red,
                    modifier = Modifier.padding(end = 8.dp)
                )
                TransactionTextView(
                    text = "Budget Alerts",
                    style = Typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            alerts.forEach { budget ->
                TransactionTextView(
                    text = "${budget.category} is ${String.format("%.1f", budget.percentageUsed)}% used",
                    color = Red,
                    style = Typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun BudgetItem(
    budget: BudgetEntity,
    onDelete: () -> Unit
) {
    val progress = (budget.currentSpending / budget.monthlyBudget).coerceIn(0.0, 1.0)
    val progressColor = when {
        budget.isExceeded() -> Red
        budget.isNearLimit() -> Yellow
        else -> Green
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background( OceanBlue.copy(alpha = 0.85f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TransactionTextView(
                text = budget.category,
                style = Typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            TransactionTextView(
                text = "${Utils.formatCurrency(budget.currentSpending)} / ${Utils.formatCurrency(budget.monthlyBudget)}",
                style = Typography.bodyMedium,
                color = SoftGold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
            color = progressColor,
            trackColor = Color.DarkGray,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TransactionTextView(
                text = "${String.format("%.1f", budget.percentageUsed)}% used",
                style = Typography.bodySmall,
                color = Teal
            )
            TransactionTextView(
                text = "${Utils.formatCurrency(budget.remainingBudget)} remaining",
                style = Typography.bodySmall,
                color = if (budget.remainingBudget < 0) Red else Teal
            )
        }
    }
}
