package ke.ac.ku.ledgerly.utils

import android.content.Context
import android.os.Environment
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment
import com.opencsv.CSVWriter
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFRow
import java.io.File
import java.io.FileWriter
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object TransactionExportManager {

    private const val EXPORT_DIR = "Ledgerly_Exports"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dateFormatShort = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getExportDirectory(context: Context): File {
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) 
            ?: throw Exception("Documents directory not available")
        val exportDir = File(documentsDir, EXPORT_DIR)
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        return exportDir
    }

    fun exportToCSV(
        context: Context,
        transactions: List<TransactionEntity>,
        fileName: String = "transactions_${System.currentTimeMillis()}.csv"
    ): File {
        val exportDir = getExportDirectory(context)
        val csvFile = File(exportDir, fileName)

        CSVWriter(FileWriter(csvFile)).use { writer ->
            // Write header
            val header = arrayOf("ID", "Category", "Amount", "Date", "Type", "Notes", "Payment Method", "Tags")
            writer.writeNext(header)

            // Write transactions
            transactions.forEach { transaction ->
                val row = arrayOf(
                    transaction.id.toString(),
                    transaction.category,
                    transaction.amount.toString(),
                    dateFormat.format(Date(transaction.date)),
                    transaction.type,
                    transaction.notes,
                    transaction.paymentMethod,
                    transaction.tags
                )
                writer.writeNext(row)
            }
        }

        return csvFile
    }

    fun exportToExcel(
        context: Context,
        transactions: List<TransactionEntity>,
        fileName: String = "transactions_${System.currentTimeMillis()}.xlsx"
    ): File {
        val exportDir = getExportDirectory(context)
        val excelFile = File(exportDir, fileName)

        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet("Transactions")

            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = listOf("ID", "Category", "Amount", "Date", "Type", "Notes", "Payment Method", "Tags")
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                val style = workbook.createCellStyle()
                val font = workbook.createFont()
                font.bold = true
                style.setFont(font)
                cell.cellStyle = style
            }

            // Create data rows
            transactions.forEachIndexed { rowIndex, transaction ->
                val row = sheet.createRow(rowIndex + 1)
                row.createCell(0).setCellValue(transaction.id?.toDouble() ?: 0.0)
                row.createCell(1).setCellValue(transaction.category)
                row.createCell(2).setCellValue(transaction.amount)
                row.createCell(3).setCellValue(dateFormat.format(Date(transaction.date)))
                row.createCell(4).setCellValue(transaction.type)
                row.createCell(5).setCellValue(transaction.notes)
                row.createCell(6).setCellValue(transaction.paymentMethod)
                row.createCell(7).setCellValue(transaction.tags)
            }

            // Set column widths manually (AWT autoSizeColumn not available on Android)
            sheet.setColumnWidth(0, 3000)   // ID
            sheet.setColumnWidth(1, 5000)   // Category
            sheet.setColumnWidth(2, 4000)   // Amount
            sheet.setColumnWidth(3, 5000)   // Date
            sheet.setColumnWidth(4, 4000)   // Type
            sheet.setColumnWidth(5, 6000)   // Notes
            sheet.setColumnWidth(6, 5000)   // Payment Method
            sheet.setColumnWidth(7, 4000)   // Tags

            // Write to file
            FileOutputStream(excelFile).use { fos ->
                workbook.write(fos)
            }
        }

        return excelFile
    }

    fun exportToPDF(
        context: Context,
        transactions: List<TransactionEntity>,
        fileName: String = "transactions_${System.currentTimeMillis()}.pdf"
    ): File {
        val exportDir = getExportDirectory(context)
        val pdfFile = File(exportDir, fileName)

        val writer = PdfWriter(pdfFile)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        // Add title
        val title = Paragraph("Transaction Report")
            .setFontSize(20f)
            .setBold()
            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
        document.add(title)

        // Add date generated
        val dateGenerated = Paragraph("Generated on: ${dateFormat.format(Date())}")
            .setFontSize(10f)
            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
        document.add(dateGenerated)

        document.add(Paragraph("\n"))

        // Create table
        val columnCount = 8
        val table = Table(floatArrayOf(1f, 2f, 1.5f, 2f, 1.5f, 2f, 2f, 1.5f))

        // Add header cells
        val headers = listOf("ID", "Category", "Amount", "Date", "Type", "Notes", "Payment Method", "Tags")
        headers.forEach { header ->
            val cell = Cell()
            cell.setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)
            cell.add(Paragraph(header).setBold())
            table.addCell(cell)
        }

        // Add data rows
        transactions.forEach { transaction ->
            table.addCell(transaction.id.toString())
            table.addCell(transaction.category)
            table.addCell("${transaction.amount}")
            table.addCell(dateFormatShort.format(Date(transaction.date)))
            table.addCell(transaction.type)
            table.addCell(transaction.notes)
            table.addCell(transaction.paymentMethod)
            table.addCell(transaction.tags)
        }

        document.add(table)

        // Add summary
        document.add(Paragraph("\n"))
        val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
        val summary = Paragraph()
        summary.add("Total Transactions: ${transactions.size}\n")
        summary.add("Total Expense: $totalExpense\n")
        summary.add("Total Income: $totalIncome\n")
        summary.add("Net: ${totalIncome - totalExpense}")
        document.add(summary)

        document.close()

        return pdfFile
    }

    fun getExportedFiles(context: Context): List<File> {
        return try {
            val exportDir = getExportDirectory(context)
            exportDir.listFiles()?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteExportedFile(file: File): Boolean {
        return file.delete()
    }
}
