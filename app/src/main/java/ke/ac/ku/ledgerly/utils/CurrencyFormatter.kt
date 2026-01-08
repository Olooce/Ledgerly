package ke.ac.ku.ledgerly.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {

    fun formatWithSymbol(
        amount: BigDecimal,
        currencyCode: String,
        locale: Locale = Locale.getDefault()
    ): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val formatter = NumberFormat.getCurrencyInstance(locale).apply {
                this.currency = currency
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            formatter.format(amount)
        } catch (e: Exception) {
            // Fallback if currency code is invalid
            formatWithCode(amount, currencyCode)
        }
    }

    fun formatWithCode(
        amount: BigDecimal,
        currencyCode: String,
        locale: Locale = Locale.getDefault()
    ): String {
        val formatter = getNumberFormatter(locale)
        return "${formatter.format(amount)} $currencyCode"
    }

    fun formatPlain(
        amount: BigDecimal,
        decimalPlaces: Int = 2,
        locale: Locale = Locale.getDefault()
    ): String {
        val formatter = getNumberFormatter(locale)
        formatter.minimumFractionDigits = decimalPlaces
        formatter.maximumFractionDigits = decimalPlaces
        return formatter.format(amount)
    }

    fun formatShort(
        amount: BigDecimal,
        currencyCode: String,
        decimalPlaces: Int = 1
    ): String {
        val absAmount = amount.abs()
        val (divisor, suffix) = when {
            absAmount >= BigDecimal(1_000_000_000) -> BigDecimal(1_000_000_000) to "B"
            absAmount >= BigDecimal(1_000_000) -> BigDecimal(1_000_000) to "M"
            absAmount >= BigDecimal(1_000) -> BigDecimal(1_000) to "K"
            else -> BigDecimal(1) to ""
        }

        val formatted = if (suffix.isEmpty()) {
            formatPlain(amount, 0)
        } else {
            val shortAmount = amount.divide(divisor, decimalPlaces, RoundingMode.HALF_UP)
            formatPlain(shortAmount, decimalPlaces)
        }

        return try {
            val symbol = Currency.getInstance(currencyCode).symbol
            "$symbol$formatted$suffix"
        } catch (e: Exception) {
            "$formatted$suffix $currencyCode"
        }
    }
    private fun getNumberFormatter(locale: Locale): DecimalFormat {
        return NumberFormat.getInstance(locale) as DecimalFormat
    }


    fun parseAmount(input: String): BigDecimal? {
        return try {
            // Remove common currency symbols and whitespace
            val cleaned = input
                .replace(Regex("[^\\d.,\\-]"), "")
                .replace(Regex("[^\\d.\\-]"), "") // Keep only digits, decimal point, and minus
            BigDecimal(cleaned)
        } catch (e: Exception) {
            null
        }
    }
    fun formatPercentage(
        percentage: BigDecimal,
        decimalPlaces: Int = 1
    ): String {
        val formatter = NumberFormat.getInstance() as DecimalFormat
        formatter.minimumFractionDigits = decimalPlaces
        formatter.maximumFractionDigits = decimalPlaces
        return "${formatter.format(percentage)}%"
    }

    @Deprecated("Use formatCurrency(amount: Double, currencyCode: String) instead", ReplaceWith("formatCurrency(amount, \"KES\")"))
    fun formatCurrency(amount: Double): String {
        return formatCurrency(amount, "KES")
    }

    @Deprecated("Use formatCurrency(amount: BigDecimal, currencyCode: String) instead", ReplaceWith("formatCurrency(amount, \"KES\")"))
    fun formatCurrency(amount: BigDecimal): String {
        return formatCurrency(amount, "KES")
    }

    fun formatCurrency(amount: Double, currencyCode: String): String {
        val formatter = NumberFormat.getCurrencyInstance()
        try {
            formatter.currency = Currency.getInstance(currencyCode)
        } catch (e: Exception) {
            // Use default currency if the provided one is invalid
            formatter.currency = Currency.getInstance("USD")
        }
        return formatter.format(amount)
    }

    fun formatCurrency(amount: BigDecimal, currencyCode: String): String {
        val formatter = NumberFormat.getCurrencyInstance()
        try {
            formatter.currency = Currency.getInstance(currencyCode)
        } catch (e: Exception) {
            // Use default currency if the provided one is invalid
            formatter.currency = Currency.getInstance("USD")
        }
        return formatter.format(amount)
    }
}