package ke.ac.ku.ledgerly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.domain.CurrencyManager
import ke.ac.ku.ledgerly.ui.theme.ChartExpenseDark
import ke.ac.ku.ledgerly.ui.theme.SuccessGreenDark
import ke.ac.ku.ledgerly.ui.widget.CircularIcon
import ke.ac.ku.ledgerly.ui.widget.ItemSurface
import ke.ac.ku.ledgerly.utils.FormatingUtils
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    currencyManager: CurrencyManager,
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    var displayAmount by remember { mutableStateOf<BigDecimal?>(null) }
    var currency by remember { mutableStateOf("") }


    LaunchedEffect(transaction, currencyManager) {
        launch {
            currency = currencyManager.getDisplayCurrency()
            displayAmount = currencyManager.convertToDisplayCurrency(
                transaction.amountUsd.toString().toBigDecimal(), currency
            ).toBigDecimal()
        }
    }

    ItemSurface(modifier) {
        CircularIcon(transaction.category, iconSize, iconTint)
        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = transaction.category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (transaction.paymentMethod.isNotEmpty()) {
                Text(
                    text = "Via ${transaction.paymentMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (transaction.tags.isNotEmpty()) {
                Text(
                    text = "Tags: ${transaction.tags}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = FormatingUtils.formatDateToHumanReadableForm(transaction.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            if (transaction.notes.isNotEmpty()) {
                Text(
                    text = transaction.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        displayAmount?.let {
            Text(
                text = "$currency ${it}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (transaction.type == "Income") SuccessGreenDark else ChartExpenseDark
            )
        }
    }
}
