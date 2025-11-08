package ke.ac.ku.ledgerly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.ui.theme.LedgerlyBlue
import ke.ac.ku.ledgerly.ui.theme.LedgerlyGreen
import ke.ac.ku.ledgerly.ui.theme.LedgerlyGreenLight
import ke.ac.ku.ledgerly.ui.widget.CircularIcon
import ke.ac.ku.ledgerly.ui.widget.ItemSurface
import ke.ac.ku.ledgerly.utils.FormatingUtils

@Composable
fun BudgetItem(
    budget: BudgetEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    val progress = (budget.currentSpending / budget.monthlyBudget.coerceAtLeast(0.01)).coerceIn(0.0, 1.0).toFloat()
    val progressColor = when {
        budget.isExceeded() -> LedgerlyBlue
        budget.isNearLimit() -> LedgerlyGreenLight
        else -> LedgerlyGreen
    }

    ItemSurface(modifier) {
        CircularIcon(budget.category, iconSize, iconTint)
        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(
                    text = budget.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${FormatingUtils.formatCurrency(budget.currentSpending)} / ${FormatingUtils.formatCurrency(budget.monthlyBudget)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.small),
                color = progressColor,
                trackColor = LedgerlyGreenLight.copy(alpha = 0.3f),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
            )

            Spacer(Modifier.height(6.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", budget.percentageUsed)}% used",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${FormatingUtils.formatCurrency(budget.remainingBudget)} remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (budget.remainingBudget < 0) LedgerlyBlue else LedgerlyGreen
                )
            }
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(ke.ac.ku.ledgerly.R.drawable.ic_delete),
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                    .padding(4.dp)
            )
        }
    }
}
