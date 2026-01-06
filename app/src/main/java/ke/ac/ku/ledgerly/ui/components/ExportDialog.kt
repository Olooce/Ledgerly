package ke.ac.ku.ledgerly.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    var selectedFormat by remember {
        mutableStateOf<ExportFormat>(ExportFormat.CSV)
    }
    var customFileName by remember { mutableStateOf("") }
    var enableEncryption by remember { mutableStateOf(false) }
    var encryptionPassword by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = "Select Export Format",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ExportOption(
                    selected = selectedFormat == ExportFormat.CSV,
                    title = "CSV (.csv)",
                    description = "Compatible with spreadsheet apps",
                    onClick = { selectedFormat = ExportFormat.CSV }
                )

                ExportOption(
                    selected = selectedFormat == ExportFormat.EXCEL,
                    title = "Excel (.xlsx)",
                    description = "Microsoft Excel format with styling",
                    onClick = { selectedFormat = ExportFormat.EXCEL }
                )

                ExportOption(
                    selected = selectedFormat == ExportFormat.PDF,
                    title = "PDF (.pdf)",
                    description = "Includes summary statistics",
                    onClick = { selectedFormat = ExportFormat.PDF }
                )

                // Custom File Name Section
                Text(
                    text = "Custom File Name (Optional)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextField(
                    value = customFileName,
                    onValueChange = { customFileName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "e.g., My Transactions",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    singleLine = true,
                    label = { Text("File name (without extension)") }
                )

                // Encryption Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = enableEncryption,
                                onClick = { enableEncryption = !enableEncryption })
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = enableEncryption,
                            onCheckedChange = { enableEncryption = it }
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = "Encrypt File",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Protect your export with encryption",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Encryption Password Field (visible only when encryption is enabled)
                    if (enableEncryption) {
import androidx.compose.ui.text.input.PasswordVisualTransformation

                        TextField(
                            value = encryptionPassword,
                            onValueChange = { encryptionPassword = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Enter a secure password",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            label = { Text("Encryption Password") },
                            singleLine = true,
                            isError = enableEncryption && encryptionPassword.isBlank(),
                            visualTransformation = PasswordVisualTransformation()
                        )
                        if (enableEncryption && encryptionPassword.isBlank()) {
                            Text(
                                text = "Password is required for encryption",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                ExportSummary(
                    transactionCount = transactions.size,
                    totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount },
                    totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            val fileName: String? =
                                if (customFileName.isBlank()) {
                                    null
                                } else {
                                    when (selectedFormat) {
                                        ExportFormat.CSV -> "${customFileName}.csv"
                                        ExportFormat.EXCEL -> "${customFileName}.xlsx"
                                        ExportFormat.PDF -> "${customFileName}.pdf"
                                        else -> {
                                            null
                                        }
                                    }
                                }

                            // Check if encryption is enabled but password is empty
                            if (enableEncryption && encryptionPassword.isBlank()) {
                                // Show error - this is handled by isError in the TextField
                                return@Button
                            }

                            exportViewModel.exportTransactions(
                                transactions,
                                selectedFormat,
                                fileName,
                                enableEncryption,
                                encryptionPassword
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !enableEncryption || encryptionPassword.isNotBlank()
                    ) {
                        Text("Export")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportOption(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExportSummary(
    transactionCount: Int,
    totalExpense: Double,
    totalIncome: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Summary",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        SummaryRow("Transactions", transactionCount.toString())
        SummaryRow(
            "Total Expense",
            String.format("%.2f", totalExpense),
            MaterialTheme.colorScheme.error
        )
        SummaryRow(
            "Total Income",
            String.format("%.2f", totalIncome),
            MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
fun ExportProgressDialog(
    progress: Int,
    isExporting: Boolean
) {
    if (!isExporting) return

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Text(
                    text = "$progress%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ExportStatusSnackbar(
    message: String?,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    if (message == null) return

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isError)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary,
        tonalElevation = 6.dp,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = if (isError)
                MaterialTheme.colorScheme.onError
            else
                MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ExportSuccessDialog(
    exportViewModel: ExportViewModel,
    file: java.io.File?,
    onDismiss: () -> Unit
) {
    if (file == null) return

    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success Icon
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                // Title
                Text(
                    text = "Export Successful",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // File Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "File Name",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            val shareIntent = exportViewModel.shareExportedFile(file)
                            context.startActivity(Intent.createChooser(shareIntent, "Share Export"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp)
                        )
                        Text("Share")
                    }
                }
            }
        }
    }
}

