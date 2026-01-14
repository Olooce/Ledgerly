package ke.ac.ku.ledgerly.presentation.budget

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import ke.ac.ku.ledgerly.data.constants.NavRouts
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.presentation.settings.SettingsViewModel
import ke.ac.ku.ledgerly.ui.components.LedgerlyTopBar
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.ui.widget.TransactionTextView
import ke.ac.ku.ledgerly.utils.CurrencyFormatter.formatCurrency
import java.math.BigDecimal

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BudgetScreen(
    navController: NavController,
    viewModel: BudgetViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val budgets by viewModel.budgets.collectAsState()
    val displayCurrency by settingsViewModel.displayCurrency.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (topBar, list, add) = createRefs()

            LedgerlyTopBar(
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ledgerly),
                    contentDescription = "Ledgerly Logo",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 24.dp)
                        .size(102.dp)
                )
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                ) {


                    Image(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(24.dp)
                            .clickable { navController.popBackStack() },
                        colorFilter = ColorFilter.tint(Color.White)
                    )

                    TransactionTextView(
                        text = "My Budgets",
                        style = Typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            if (budgets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .constrainAs(list) {
                            top.linkTo(topBar.bottom)
                            bottom.linkTo(parent.bottom)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_empty_state),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            colorFilter = ColorFilter.tint(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.5f
                                )
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No budgets set yet",
                            style = Typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .constrainAs(list) {
                            top.linkTo(topBar.bottom, margin = 24.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                            height = Dimension.fillToConstraints
                        },
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(budgets) { budget ->
                        BudgetItem(budget, displayCurrency)
                    }
                }
            }

            FloatingActionButton(
                onClick = { navController.navigate(NavRouts.addBudget) },
                modifier = Modifier
                    .padding(16.dp)
                    .constrainAs(add) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    },
                containerColor = Color(0xFF009650),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Budget",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun BudgetItem(
    budget: BudgetEntity,
    currency: String
) {
    val progress = budget.percentageUsed.divide(BigDecimal(100)).toFloat()
    val isOverBudget = budget.isExceeded()
    val progressColor = if (isOverBudget) Color(0xFFEF4444) else Color(0xFF10B981)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(progressColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_budget),
                            contentDescription = null,
                            tint = progressColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = budget.category,
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = budget.monthYear,
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(budget.monthlyBudget, currency),
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Limit",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.1f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(budget.currentSpending, currency),
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOverBudget) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Remaining",
                        style = Typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(
                            budget.remainingBudget.max(BigDecimal.ZERO),
                            currency
                        ),
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOverBudget) Color(0xFFEF4444) else Color(0xFF10B981)
                    )
                }
            }
        }
    }
}
