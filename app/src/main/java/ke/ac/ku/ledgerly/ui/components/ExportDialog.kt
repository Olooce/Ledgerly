package ke.ac.ku.ledgerly.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.presentation.transactions.ExportFormat
import ke.ac.ku.ledgerly.presentation.transactions.ExportViewModel

@Composable
fun ExportDialog(
    transactions: List<TransactionEntity>,
    exportViewModel: ExportViewModel,
    onDismiss: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf<ExportFormat>(ExportFormat.CSV) }
    var customFileName by remember { mutableStateOf("") }
    var showFileNameInput by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Export Transactions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Export Format Selection
            Text(
                text = "Select Export Format:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            )

            // CSV Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedFormat == ExportFormat.CSV,
                        onClick = { selectedFormat = ExportFormat.CSV }
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedFormat == ExportFormat.CSV,
                    onClick = { selectedFormat = ExportFormat.CSV }
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "CSV (.csv)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Comma-separated values, compatible with spreadsheet apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Excel Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedFormat == ExportFormat.EXCEL,
                        onClick = { selectedFormat = ExportFormat.EXCEL }
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedFormat == ExportFormat.EXCEL,
                    onClick = { selectedFormat = ExportFormat.EXCEL }
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "Excel (.xlsx)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Microsoft Excel format with formatting and styling",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // PDF Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedFormat == ExportFormat.PDF,
                        onClick = { selectedFormat = ExportFormat.PDF }
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedFormat == ExportFormat.PDF,
                    onClick = { selectedFormat = ExportFormat.PDF }
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "PDF (.pdf)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Portable Document Format, includes summary statistics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Export Summary
            ExportSummary(
                transactionCount = transactions.size,
                totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount },
                totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount },
                modifier = Modifier.padding(top = 16.dp)
            )

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val fileName = when (selectedFormat) {
                            ExportFormat.CSV -> if (customFileName.isNotBlank()) "$customFileName.csv" else null
                            ExportFormat.EXCEL -> if (customFileName.isNotBlank()) "$customFileName.xlsx" else null
                            ExportFormat.PDF -> if (customFileName.isNotBlank()) "$customFileName.pdf" else null
                        }
                        exportViewModel.exportTransactions(transactions, selectedFormat, fileName)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Export")
                }
            }
        }
    }
}

@Composable
private fun ExportSummary(
    transactionCount: Int,
    totalExpense: Double,
    totalIncome: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Summary",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Transactions:",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = transactionCount.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total Expense:",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = String.format("%.2f", totalExpense),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.Red
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total Income:",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = String.format("%.2f", totalIncome),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.Green
            )
        }
    }
}

@Composable
fun ExportProgressDialog(
    progress: Int,
    isExporting: Boolean,
    modifier: Modifier = Modifier
) {
    if (isExporting) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Exporting Transactions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    strokeWidth = 4.dp
                )

                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Text(
                    text = "$progress%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ExportStatusSnackbar(
    message: String?,
    isError: Boolean = false,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (message != null) {
        Snackbar(
            modifier = modifier.padding(16.dp),
            containerColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            contentColor = if (isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
        ) {
            Text(message)
        }
    }
}
