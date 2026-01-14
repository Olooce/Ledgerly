package ke.ac.ku.ledgerly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.domain.CurrencyManager
import ke.ac.ku.ledgerly.ui.theme.ChartExpenseDark
import ke.ac.ku.ledgerly.ui.theme.SuccessGreenDark
import ke.ac.ku.ledgerly.ui.widget.CircularIcon
import ke.ac.ku.ledgerly.ui.widget.ItemSurface
import ke.ac.ku.ledgerly.utils.FormatingUtils
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun RecurringTransactionItem(
    recurring: RecurringTransactionEntity,
    currencyManager: CurrencyManager,
    onToggleActive: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = recurring.type == "Income"
    val amountColor = if (isIncome) SuccessGreenDark else ChartExpenseDark

    var displayAmount by remember { mutableStateOf<BigDecimal?>(null) }
    var currency by remember { mutableStateOf("") }

    LaunchedEffect(recurring, currencyManager) {
        launch {
            currency = currencyManager.getDisplayCurrency()
            displayAmount = currencyManager.convertToDisplayCurrency(recurring.amountUsd, currency)
                .toBigDecimal()
        }
    }

    ItemSurface(modifier) {
        CircularIcon(
            recurring.category,
            iconSize = 60.dp,
            iconTint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))

        Column(modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = recurring.category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            displayAmount?.let {
                Text(
                    text = "$currency $it",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = amountColor
                )
            }
            if (recurring.paymentMethod.isNotEmpty()) {
                Text(
                    text = "Via ${recurring.paymentMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "Frequency: ${
                    recurring.frequency.name.lowercase().replaceFirstChar { it.uppercase() }
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = buildString {
                    append("Start: ${FormatingUtils.formatDateToHumanReadableForm(recurring.startDate)}")
                    recurring.endDate?.let {
                        append(
                            " • End: ${
                                FormatingUtils.formatDateToHumanReadableForm(
                                    it
                                )
                            }"
                        )
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Switch(
                checked = recurring.isActive,
                enabled = recurring.id != null,
                onCheckedChange = { isActive ->
                    recurring.id?.let { onToggleActive(it, isActive) }
                }
            )
            IconButton(
                onClick = { recurring.id?.let(onDelete) },
                enabled = recurring.id != null
            ) {
                Icon(
                    painter = painterResource(ke.ac.ku.ledgerly.R.drawable.ic_delete),
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
