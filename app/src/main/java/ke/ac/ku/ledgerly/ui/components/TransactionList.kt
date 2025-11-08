package ke.ac.ku.ledgerly.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.ui.theme.Green
import ke.ac.ku.ledgerly.ui.theme.Red
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.ui.widget.TransactionItem
import ke.ac.ku.ledgerly.ui.widget.TransactionTextView
import ke.ac.ku.ledgerly.utils.FormatingUtils
import ke.ac.ku.ledgerly.utils.Utils

@Composable
fun TransactionList(
    modifier: Modifier,
    list: List<TransactionEntity>,
    title: String = "Recent Transactions",
    onSeeAllClicked: () -> Unit
) {
    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
        item {
            Column {
                Box(modifier = modifier.fillMaxWidth()) {
                    TransactionTextView(
                        text = title,
                        style = Typography.titleLarge,
                    )
                    if (title == "Recent Transactions") {
                        TransactionTextView(
                            text = "See all",
                            style = Typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable {
                                    onSeeAllClicked.invoke()
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.size(12.dp))
            }
        }
        items(
            items = list,
            key = { item -> item.id ?: 0 }) { item ->
            val icon = Utils.getItemIcon(item.category)

            TransactionItem(
                title = item.category,
                amount = FormatingUtils.formatCurrency(item.amount),
                icon = icon,
                date = FormatingUtils.formatDayMonth(item.date),
                paymentMethod = item.paymentMethod,
                notes = item.notes,
                tags = item.tags,
                color = if (item.type == "Income") Green else Red,
                modifier = Modifier
            )
        }
    }
}