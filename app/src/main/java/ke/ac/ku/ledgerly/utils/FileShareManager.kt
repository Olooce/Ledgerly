package ke.ac.ku.ledgerly.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

object FileShareManager {

    private const val TAG = "FileShareManager"

    fun shareFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = when {
                    file.name.endsWith(".csv") -> "text/csv"
                    file.name.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    file.name.endsWith(".pdf") -> "application/pdf"
                    else -> "*/*"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val shareIntent = Intent.createChooser(intent, "Share Export File")
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file: ${e.message}", e)
        }
    }

    fun openFile(context: Context, file: File): Result<Unit> {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    uri,
                    when {
                        file.name.endsWith(".csv") -> "text/csv"
                        file.name.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        file.name.endsWith(".pdf") -> "application/pdf"
                        else -> "*/*"
                    }
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Check if there is an app available to handle the intent
            val resolveActivity = intent.resolveActivity(context.packageManager)
            if (resolveActivity == null) {
                return Result.failure(
                    IllegalStateException("No app available to open file with MIME type: ${intent.type}")
                )
            }

            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening file: ${e.message}", e)
            Result.failure(e)
        }
    }
}
