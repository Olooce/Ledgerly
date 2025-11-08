package ke.ac.ku.ledgerly.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ke.ac.ku.ledgerly.ui.theme.Typography

@Composable
fun AmountRangeFilter(
    amountRange: ClosedFloatingPointRange<Double>?,
    onAmountRangeChange: (ClosedFloatingPointRange<Double>?) -> Unit,
    modifier: Modifier = Modifier
) {
    var minAmount by remember { mutableStateOf(amountRange?.start?.toString() ?: "") }
    var maxAmount by remember { mutableStateOf(amountRange?.endInclusive?.toString() ?: "") }
    
    LaunchedEffect(amountRange) {
        minAmount = amountRange?.start?.toString() ?: ""
        maxAmount = amountRange?.endInclusive?.toString() ?: ""
    }

    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        TransactionTextView(
            text = "Amount Range",
            style = Typography.bodySmall,
            color = primaryTextColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minAmount,
                onValueChange = {
                    minAmount = it
                    updateAmountRange(minAmount, maxAmount, onAmountRangeChange)
                },
                modifier = Modifier.weight(1f),
                placeholder = {
                    TransactionTextView(
                        text = "Min",
                        style = Typography.bodySmall,
                        color = secondaryTextColor
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor
                ),
                textStyle = Typography.bodyMedium.copy(color = primaryTextColor)
            )

            OutlinedTextField(
                value = maxAmount,
                onValueChange = {
                    maxAmount = it
                    updateAmountRange(minAmount, maxAmount, onAmountRangeChange)
                },
                modifier = Modifier.weight(1f),
                placeholder = {
                    TransactionTextView(
                        text = "Max",
                        style = Typography.bodySmall,
                        color = secondaryTextColor
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = primaryTextColor,
                    unfocusedTextColor = primaryTextColor
                ),
                textStyle = Typography.bodyMedium.copy(color = primaryTextColor)
            )
        }
    }
}

private fun updateAmountRange(
    minText: String,
    maxText: String,
    onAmountRangeChange: (ClosedFloatingPointRange<Double>?) -> Unit
) {
    val min = minText.toDoubleOrNull()
    val max = maxText.toDoubleOrNull()

    when {
        min != null && max != null && min <= max -> onAmountRangeChange(min..max)
        min != null && maxText.isEmpty() -> onAmountRangeChange(min..Double.MAX_VALUE)
        max != null && minText.isEmpty() -> onAmountRangeChange(0.0..max)
        minText.isEmpty() && maxText.isEmpty() -> onAmountRangeChange(null)
        else -> {}
    }
}