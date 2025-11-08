package ke.ac.ku.ledgerly.utils

import ke.ac.ku.ledgerly.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


object Utils {
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

    fun getTimeBasedGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    fun getCurrentMonthYear(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return String.format("%04d-%02d", year, month)
    }

}