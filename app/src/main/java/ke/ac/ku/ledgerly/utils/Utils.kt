package ke.ac.ku.ledgerly.utils

import ke.ac.ku.ledgerly.R
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale


object Utils {

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

    fun formatStringDateToMonthDayYear(date: String): String {
        val millis = getMillisFromDate(date)
        return formatDayMonthYear(millis)
    }

    fun getMillisFromDate(date: String): Long {
        return getMilliFromDate(date)
    }

    fun getMilliFromDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L

        val formats = listOf(
            "dd/MM/yyyy",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        )

        for (format in formats) {
            try {
                val formatter = SimpleDateFormat(format, Locale.getDefault())
                val date = formatter.parse(dateStr)
                if (date != null) return date.time
            } catch (e: ParseException) {
                // Ignore and try next format
            }
        }

        throw IllegalArgumentException("Unable to parse date: $dateStr. Expected formats: dd/MM/yyyy, yyyy-MM-dd'T'HH:mm:ss, or yyyy-MM-dd")
    }


    fun getItemIcon(category: String): Int {
        return when (category) {
            "Paypal" -> {
                R.drawable.ic_paypal
            }
            "Netflix" -> {
                R.drawable.ic_netflix
            }
            "Starbucks" -> {
                R.drawable.ic_starbucks
            }
            "Freelance" -> {
                R.drawable.ic_upwork
            }
            "Budget" -> {
                R.drawable.ic_budget
            }
            "Education" -> {
                R.drawable.ic_education
            }
            "Entertainment" -> {
                R.drawable.ic_ent
            }

            "Grocery" -> {
                R.drawable.ic_grocery
            }

            "Healthcare" -> {
                R.drawable.ic_healthcare
            }

            "Investments" -> {
                R.drawable.ic_investment
            }
            "Receipt" -> {
                R.drawable.ic_receipt
            }

            "Rent" -> {
                R.drawable.ic_rent
            }
            "Transport" -> {
                R.drawable.ic_transport
            }

            "Utilities" -> {
                R.drawable.ic_utility
            }

            else -> {
                R.drawable.ic_default_category
            }
        }
    }

    fun getCurrentMonthYear(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return String.format("%04d-%02d", year, month)
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