package ke.ac.ku.ledgerly.feature.recurring

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.ui.theme.DeepNavy
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.ui.theme.White
import ke.ac.ku.ledgerly.ui.theme.Zinc
import ke.ac.ku.ledgerly.utils.Utils
import ke.ac.ku.ledgerly.widget.TransactionTextView

@Composable
fun RecurringTransactionsScreen(
    navController: NavController,
    viewModel: RecurringTransactionViewModel = hiltViewModel()
) {
    val recurringTransactions by viewModel.recurringTransactions.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(padding)
        ) {
            val (topBar, header, content) = createRefs()

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

                TransactionTextView(
                    text = "Recurring Transactions",
                    style = Typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Content
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
                                onToggleActive = { id, isActive ->
                                    viewModel.toggleRecurringTransactionStatus(id, isActive)
                                },
                                onDelete = { id ->
                                    viewModel.deleteRecurringTransaction(id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringTransactionItem(
    recurring: RecurringTransactionEntity,
    onToggleActive: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TransactionTextView(
                    text = recurring.category,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                TransactionTextView(
                    text = Utils.formatCurrency(recurring.amount),
                    fontSize = 14.sp,
                    color = if (recurring.type == "Income") Color(0xFF2E7D32) else Color(0xFFC62828)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(
                        text = recurring.frequency.name,
                        backgroundColor = Zinc
                    )
                    if (recurring.paymentMethod.isNotEmpty()) {
                        Chip(
                            text = recurring.paymentMethod,
                            backgroundColor = Color(0xFFE3F2FD)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                TransactionTextView(
                    text = "Start: ${recurring.startDate}${
                        recurring.endDate?.let { " • End: $it" } ?: ""
                    }",
                    fontSize = 12.sp,
                    color = DeepNavy
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = recurring.isActive,
                    onCheckedChange = { isActive ->
                        recurring.id?.let { onToggleActive(it, isActive) }
                    }
                )

                IconButton(onClick = { recurring.id?.let { onDelete(it) } }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun Chip(text: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        TransactionTextView(
            text = text,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}