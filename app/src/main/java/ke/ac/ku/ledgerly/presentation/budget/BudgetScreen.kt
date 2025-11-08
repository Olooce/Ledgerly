package ke.ac.ku.ledgerly.presentation.budget

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.presentation.add_transaction.AddTransactionViewModel
import ke.ac.ku.ledgerly.ui.theme.ErrorRed
import ke.ac.ku.ledgerly.ui.theme.LedgerlyAccent
import ke.ac.ku.ledgerly.ui.theme.LedgerlyBlue
import ke.ac.ku.ledgerly.ui.theme.LedgerlyGreen
import ke.ac.ku.ledgerly.ui.theme.LedgerlyGreenLight
import ke.ac.ku.ledgerly.ui.theme.WarningYellow
import ke.ac.ku.ledgerly.ui.theme.Zinc
import ke.ac.ku.ledgerly.ui.components.BudgetItem
import ke.ac.ku.ledgerly.utils.FormatingUtils
import ke.ac.ku.ledgerly.utils.Utils

@Composable
fun BudgetScreen(
    navController: NavController,
    viewModel: BudgetViewModel = hiltViewModel(),
    addTransactionViewModel: AddTransactionViewModel = hiltViewModel()
) {
    val budgets by viewModel.budgets.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBudgets()
        viewModel.loadAlerts()
    }

    LaunchedEffect(Unit) {
        addTransactionViewModel.transactionAdded.collect {
            viewModel.loadBudgets()
            viewModel.loadAlerts()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (topBar, header, list, fab) = createRefs()

            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(topBar) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_topbar),
                    contentDescription = "Top Bar",
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
                // Back icon
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
                    Text(
                        text = "Budgets Overview",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = LedgerlyAccent
                    )
                    Text(
                        text = FormatingUtils.formatMonthYear(Utils.getCurrentMonthYear()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LedgerlyAccent
                    )
                }

            }

            // Budget List Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .constrainAs(list) {
                        top.linkTo(header.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    }
            ) {
                if (alerts.isNotEmpty()) {
                    AlertSection(alerts)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                when (uiState) {
                    is BudgetUiState.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = LedgerlyGreen) }

                    is BudgetUiState.Error -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as BudgetUiState.Error).message,
                            color = LedgerlyBlue
                        )
                    }

                    else -> {
                        if (budgets.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No budgets set for ${
                                        FormatingUtils.formatMonthYear(
                                            Utils.getCurrentMonthYear()
                                        )
                                    }",
                                    color = LedgerlyGreenLight
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

            // Floating Action Button
            Box(
                modifier = Modifier
                    .constrainAs(fab) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    },
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate("add_budget") },
                    containerColor = Zinc,
                    contentColor = LedgerlyAccent,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_addbutton),
                        contentDescription = "Add Transaction",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertSection(alerts: List<BudgetEntity>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        color = LedgerlyBlue.copy(alpha = 0.15f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = ErrorRed,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Budget Alerts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LedgerlyGreen
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            alerts.forEach { budget ->
                Text(
                    text = "${budget.category} is ${
                        String.format(
                            "%.1f",
                            budget.percentageUsed
                        )
                    }% used",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarningYellow
                )
            }
        }
    }
}



