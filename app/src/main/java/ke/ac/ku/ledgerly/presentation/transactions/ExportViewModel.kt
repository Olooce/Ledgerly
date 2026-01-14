package ke.ac.ku.ledgerly.presentation.transactions

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.service.NotificationService
import ke.ac.ku.ledgerly.utils.TransactionExportManager
import ke.ac.ku.ledgerly.utils.sendExportSuccessNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ExportFormat {
    object CSV : ExportFormat()
    object EXCEL : ExportFormat()
    object PDF : ExportFormat()
}

data class ExportState(
    val isExporting: Boolean = false,
    val exportProgress: Int = 0,
    val exportedFile: File? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val availableFiles: List<File> = emptyList(),
    val showShareButton: Boolean = false,
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _exportState = MutableStateFlow(ExportState())
    val exportState = _exportState.asStateFlow()

    init {
        loadAvailableExportedFiles()
    }

    fun exportTransactions(
        transactions: List<TransactionEntity>,
        format: ExportFormat,
        customFileName: String? = null,
        enableEncryption: Boolean = false,
        encryptionPassword: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _exportState.update {
                    it.copy(
                        isExporting = true,
                        errorMessage = null,
                        successMessage = null,
                        showShareButton = false
                    )
                }

                val password = if (enableEncryption && encryptionPassword.isNotEmpty()) {
                    encryptionPassword
                } else {
                    null
                }

                val file = when (format) {
                    ExportFormat.CSV -> {
                        _exportState.update { it.copy(exportProgress = 33) }
                        TransactionExportManager.exportToCSV(
                            context,
                            transactions,
                            customFileName ?: "transactions_${System.currentTimeMillis()}.csv",
                            password
                        )
                    }

                    ExportFormat.EXCEL -> {
                        _exportState.update { it.copy(exportProgress = 66) }
                        TransactionExportManager.exportToExcel(
                            context,
                            transactions,
                            customFileName ?: "transactions_${System.currentTimeMillis()}.xlsx",
                            password
                        )
                    }

                    ExportFormat.PDF -> {
                        _exportState.update { it.copy(exportProgress = 50) }
                        TransactionExportManager.exportToPDF(
                            context,
                            transactions,
                            customFileName ?: "transactions_${System.currentTimeMillis()}.pdf",
                            password
                        )
                    }
                }

                notificationService.sendExportSuccessNotification(file.name)

                _exportState.update {
                    it.copy(
                        isExporting = false,
                        exportProgress = 100,
                        exportedFile = file,
                        successMessage = "Export successful: ${file.name}",
                        errorMessage = null,
                        showShareButton = true
                    )
                }

                loadAvailableExportedFiles()
            } catch (e: Exception) {
                _exportState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = "Export failed: ${e.message}",
                        successMessage = null,
                        showShareButton = false
                    )
                }
            }
        }
    }

    fun loadAvailableExportedFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val files = TransactionExportManager.getExportedFiles(context)
                _exportState.update {
                    it.copy(availableFiles = files)
                }
            } catch (e: Exception) {
                _exportState.update {
                    it.copy(errorMessage = "Failed to load exported files: ${e.message}")
                }
            }
        }
    }

    fun deleteExportedFile(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (TransactionExportManager.deleteExportedFile(file)) {
                    loadAvailableExportedFiles()
                    _exportState.update {
                        it.copy(successMessage = "File deleted: ${file.name}")
                    }
                } else {
                    _exportState.update {
                        it.copy(errorMessage = "Failed to delete file")
                    }
                }
            } catch (e: Exception) {
                _exportState.update {
                    it.copy(errorMessage = "Delete failed: ${e.message}")
                }
            }
        }
    }

    fun clearMessages() {
        _exportState.update {
            it.copy(
                errorMessage = null,
                successMessage = null,
                exportProgress = 0,
                showShareButton = false
            )
        }
    }

    fun shareExportedFile(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = getMimeType(file)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun getMimeType(file: File): String {
        return when {
            file.name.endsWith(".csv") -> "text/csv"
            file.name.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            file.name.endsWith(".pdf") -> "application/pdf"
            file.name.endsWith(".zip") -> "application/zip"
            else -> "application/octet-stream"
        }
    }
}