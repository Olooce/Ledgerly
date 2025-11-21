package ke.ac.ku.ledgerly.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.ui.widget.TransactionTextView
import ke.ac.ku.ledgerly.utils.FormatingUtils

@Composable
fun TransactionList(
    modifier: Modifier,
    list: List<TransactionEntity>,
    title: String = "Recent Transactions",
    onSeeAllClicked: () -> Unit
) {

    if (list.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_empty_state),
                    contentDescription = "No transactions",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 16.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                )
                TransactionTextView(
                    text = "No transactions yet",
                    style = Typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    } else {
        LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 16.dp, bottom = 12.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TransactionTextView(
                            text = title,
                            style = Typography.titleMedium,
                        )
                        if (title == "Recent Transactions") {
                            TransactionTextView(
                                text = "See all",
                                style = Typography.bodySmall,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .clickable { onSeeAllClicked() }
                            )
                        }
                    }
                }
            }


            items(
                items = list,
                key = { item -> item.id ?: 0 }) { item ->
                TransactionItem(
                    title = item.category,
                    amount = FormatingUtils.formatCurrency(item.amount),
                    date = FormatingUtils.formatDateToHumanReadableForm(item.date),
                    paymentMethod = item.paymentMethod,
                    notes = item.notes,
                    tags = item.tags,
                    color = if (item.type == "Income") Color(0xFF2E7D32) else Color(0xFFC62828),
                    modifier = Modifier
                )
            }
        }
    }
}