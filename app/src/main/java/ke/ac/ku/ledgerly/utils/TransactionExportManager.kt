package ke.ac.ku.ledgerly.utils

import android.content.Context
import android.os.Environment
import com.itextpdf.kernel.pdf.EncryptionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.opencsv.CSVWriter
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.presentation.transactions.ExportFormat
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.apache.poi.poifs.crypt.EncryptionInfo
import org.apache.poi.poifs.crypt.EncryptionMode
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TransactionExportManager {

    private const val EXPORT_DIR = "Ledgerly_Exports"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun export(
        context: Context,
        transactions: List<TransactionEntity>,
        format: ExportFormat,
        fileName: String,
        password: String? = null
    ): File {
        return when (format) {
            ExportFormat.PDF -> exportPdf(context, transactions, fileName, password)
            ExportFormat.EXCEL -> exportExcel(context, transactions, fileName, password)
            ExportFormat.CSV -> exportCsv(context, transactions, fileName, password)
        }
    }

    fun exportToCSV(
        context: Context,
        transactions: List<TransactionEntity>,
        fileName: String,
        password: String? = null
    ): File {
        return exportCsv(context, transactions, fileName, password)
    }

    fun exportToExcel(
        context: Context,
        transactions: List<TransactionEntity>,
        fileName: String,
        password: String? = null
    ): File {
        return exportExcel(context, transactions, fileName, password)
    }

    fun exportToPDF(
        context: Context,
        transactions: List<TransactionEntity>,
        fileName: String,
        password: String? = null
    ): File {
        return exportPdf(context, transactions, fileName, password)
    }

    fun getExportedFiles(context: Context): List<File> {
        val exportDir = getExportDir(context)
        return exportDir.listFiles()?.toList()?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun deleteExportedFile(file: File): Boolean {
        return file.exists() && file.delete()
    }

    private fun exportPdf(
        context: Context,
        transactions: List<TransactionEntity>,
        fileName: String,
        password: String?
    ): File {
        val file = File(getExportDir(context), fileName)

        val writerProps = WriterProperties().apply {
            if (!password.isNullOrBlank()) {
                setStandardEncryption(
                    password.toByteArray(),
                    password.toByteArray(),
                    EncryptionConstants.ALLOW_PRINTING,
                    EncryptionConstants.ENCRYPTION_AES_128
                )
            }
        }

        val pdfWriter = PdfWriter(file.absolutePath, writerProps)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        // Header
        document.add(Paragraph("Transaction Report").setBold().setFontSize(18f))
        document.add(Paragraph("Generated: ${dateFormat.format(Date())}\n"))

        // Table
        val table = Table(floatArrayOf(2f, 2f, 2f, 2f))
        listOf("Category", "Amount", "Type", "Date").forEach {
            table.addHeaderCell(Cell().add(Paragraph(it).setBold()))
        }

        transactions.forEach {
            table.addCell(it.category)
            table.addCell(it.amount.toString())
            table.addCell(it.type)
            table.addCell(dateFormat.format(Date(it.date)))
        }

        document.add(table)
        document.close()

        return file
    }

    private fun exportExcel(
        context: Context,
        transactions: List<TransactionEntity>,
        fileName: String,
        password: String?
    ): File {
        val tempFile = File(getExportDir(context), "tmp_$fileName")
        val finalFile = File(getExportDir(context), fileName)

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transactions")

        // Header row
        val header = sheet.createRow(0)
        listOf("Category", "Amount", "Type", "Date").forEachIndexed { i, h ->
            header.createCell(i).setCellValue(h)
        }

        // Data rows
        transactions.forEachIndexed { i, t ->
            val row = sheet.createRow(i + 1)
            row.createCell(0).setCellValue(t.category)
            row.createCell(1).setCellValue(t.amount)
            row.createCell(2).setCellValue(t.type)
            row.createCell(3).setCellValue(dateFormat.format(Date(t.date)))
        }

        // Write unencrypted workbook to temp file
        FileOutputStream(tempFile).use { workbook.write(it) }
        workbook.close()

        // If no password, just rename and return
        if (password.isNullOrBlank()) {
            if (!tempFile.renameTo(finalFile)) {
                // Fallback: copy content if rename fails (e.g., cross-filesystem)
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }
            return finalFile
        }

        // Apply Excel encryption
        FileOutputStream(finalFile).use { fos ->
            val fs = POIFSFileSystem()
            val encryptionInfo = EncryptionInfo(EncryptionMode.standard)
            val encryptor = encryptionInfo.encryptor
            encryptor.confirmPassword(password)

            val dataStream = encryptor.getDataStream(fs)
            FileInputStream(tempFile).use { fis ->
                fis.copyTo(dataStream)
            }
            dataStream.close()

            fs.writeFilesystem(fos)
            fs.close()
        }

        tempFile.delete()
        return finalFile
    }

    private fun exportCsv(
        context: Context,
        transactions: List<TransactionEntity>,
        fileName: String,
        password: String?
    ): File {
        val csvFile = File(getExportDir(context), fileName)

        // Write CSV data
        CSVWriter(FileWriter(csvFile)).use {
            it.writeNext(arrayOf("Category", "Amount", "Type", "Date"))
            transactions.forEach { t ->
                it.writeNext(
                    arrayOf(
                        t.category,
                        t.amount.toString(),
                        t.type,
                        dateFormat.format(Date(t.date))
                    )
                )
            }
        }

        if (password.isNullOrBlank()) return csvFile

        val zipFile = File(csvFile.parent, "${csvFile.nameWithoutExtension}.zip")

        val params = ZipParameters().apply {
            isEncryptFiles = true
            encryptionMethod = EncryptionMethod.AES
        }

        ZipFile(zipFile, password.toCharArray()).use { zip ->
            zip.addFile(csvFile, params)
        }
        if (!csvFile.delete()) {
            // Security: ensure unencrypted file is removed
            csvFile.deleteOnExit()
        }

        return zipFile
    }

    private fun getExportDir(context: Context): File =
        File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            EXPORT_DIR
        ).apply { mkdirs() }
}