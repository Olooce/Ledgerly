package ke.ac.ku.ledgerly.utils

import ke.ac.ku.ledgerly.utils.Utils.getMillisFromDate
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Locale

object FormatingUtils {
    fun formatDateToHumanReadableForm(dateInMillis: Long): String {
        val dateFormatter = SimpleDateFormat("dd/MM/YYYY", Locale.getDefault())
        return dateFormatter.format(dateInMillis)
    }

    fun formatDateToISO(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    fun formatDateForChart(dateInMillis: Long): String {
        val dateFormatter = SimpleDateFormat("dd-MMM", Locale.getDefault())
        return dateFormatter.format(dateInMillis)
    }

    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "KE"))
        formatter.currency = Currency.getInstance("KES")
        val formatted = formatter.format(amount)
        return formatted.replace("KSh", "KSh ")
    }

    fun formatDayMonthYear(dateInMillis: Long): String {
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormatter.format(dateInMillis)
    }

    fun formatDayMonth(dateInMillis: Long): String {
        val dateFormatter = SimpleDateFormat("dd/MMM", Locale.getDefault())
        return dateFormatter.format(dateInMillis)
    }

    fun formatToDecimalValue(d: Double): String {
        return String.format("%.2f", d)
    }

    fun formatMonthString(monthYear: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return try {
            val date = inputFormat.parse(monthYear)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            monthYear
        }
    }

    fun formatStringDateToMonthDayYear(date: String): String {
        val millis = getMillisFromDate(date)
        return formatDayMonthYear(millis)
    }

    fun formatMonthYear(monthYear: String): String {
        val parts = monthYear.split("-")
        if (parts.size != 2) return monthYear

        val year = parts[0]
        val month = parts[1].toIntOrNull() ?: return monthYear

        val monthName = when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Unknown"
        }

        return "$monthName $year"
    }
}