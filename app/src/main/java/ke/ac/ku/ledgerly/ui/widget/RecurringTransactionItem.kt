package ke.ac.ku.ledgerly.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.utils.FormatingUtils
import ke.ac.ku.ledgerly.utils.Utils

@Composable
fun RecurringTransactionItem(
    recurring: RecurringTransactionEntity,
    onToggleActive: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Utils.getItemIcon(recurring.category)),
                    contentDescription = null,
                    tint = if (recurring.type == "Income")
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = recurring.category,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = FormatingUtils.formatCurrency(recurring.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (recurring.type == "Income")
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )

                if (recurring.paymentMethod.isNotEmpty()) {
                    Text(
                        text = "Via ${recurring.paymentMethod}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "Frequency: ${recurring.frequency.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Text(
                    text = buildString {
                        append("Start: ${FormatingUtils.formatDateToHumanReadableForm(recurring.startDate)}")
                        recurring.endDate?.let { append(" • End: ${FormatingUtils.formatDateToHumanReadableForm(it)}") }
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

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

                IconButton(onClick = { recurring.id?.let { onDelete(it) } },  enabled = recurring.id != null) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
