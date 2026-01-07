package ke.ac.ku.ledgerly.ui.components

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.model.SavingsGoalEntity
import ke.ac.ku.ledgerly.ui.theme.LedgerlyGreen
import ke.ac.ku.ledgerly.ui.theme.progressColor
import ke.ac.ku.ledgerly.utils.FormatingUtils

@Composable
fun SavingsGoalItem(
    goal: SavingsGoalEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onAddProgress: ((Double) -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddProgressDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    val progress = goal.progressPercentage.coerceIn(0.0, 100.0)
    val progressBarColor = progressColor(progress)

    val goalColor = try {
        Color(goal.color.toColorInt())
    } catch (e: Exception) {
        LedgerlyGreen
    }

    val context = LocalContext.current

    // Safe icon loading
    val iconPainter = rememberSafeIconPainter(
        iconRes = goal.icon,
        defaultIconRes = R.drawable.ic_target,
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(goalColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        iconPainter?.let { painter ->
                            Icon(
                                painter = painter,
                                contentDescription = null,
                                tint = goalColor,
                                modifier = Modifier.size(18.dp)
                            )
                        } ?: run {
                            // Fallback icon
                            Icon(
                                painter = painterResource(R.drawable.ic_target),
                                contentDescription = null,
                                tint = goalColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )

                        if (goal.description.isNotEmpty()) {
                            Text(
                                text = goal.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved: ${FormatingUtils.formatCurrency(goal.currentAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Target: ${FormatingUtils.formatCurrency(goal.targetAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Text(
                    text = "${progress.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = goalColor
                )
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (progress / 100f).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = progressBarColor,
                trackColor = progressBarColor.copy(alpha = 0.2f),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${FormatingUtils.formatCurrency(goal.remainingAmount)} left",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )

                when {
                    goal.isCompleted -> {
                        Badge(
                            containerColor = LedgerlyGreen.copy(alpha = 0.15f),
                            contentColor = LedgerlyGreen
                        ) {
                            Text(
                                "Completed",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    goal.isOnTrack -> {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "On Track",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Action buttons (only show if not completed)
            if (!goal.isCompleted && (onAddProgress != null || onComplete != null)) {
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onAddProgress != null) {
                        OutlinedButton(
                            onClick = { showAddProgressDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Add Progress",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    if (onComplete != null && progress >= 100.0) {
                        Button(
                            onClick = { showCompleteDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LedgerlyGreen
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Complete",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddProgressDialog && onAddProgress != null) {
        AddProgressDialog(
            goal = goal,
            onDismiss = { showAddProgressDialog = false },
            onConfirm = onAddProgress
        )
    }

    if (showCompleteDialog && onComplete != null) {
        CompleteGoalDialog(
            goal = goal,
            onDismiss = { showCompleteDialog = false },
            onConfirm = onComplete
        )
    }
}

@Composable
fun rememberSafeIconPainter(
    @DrawableRes iconRes: Int,
    @DrawableRes defaultIconRes: Int = R.drawable.ic_target
): Painter {
    val context = LocalContext.current

    val safeIconRes = remember(iconRes) {
        try {
            val res = context.resources
            val type = res.getResourceTypeName(iconRes)

            when (type) {
                "drawable" -> iconRes
                else -> {
                    Log.w(
                        "SafeIconPainter",
                        "Invalid icon resource type: $type (id=$iconRes)"
                    )
                    defaultIconRes
                }
            }
        } catch (e: Exception) {
            defaultIconRes
        }
    }

    return painterResource(id = safeIconRes)
}




