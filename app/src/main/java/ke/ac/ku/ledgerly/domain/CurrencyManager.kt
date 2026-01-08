
package ke.ac.ku.ledgerly.domain

import android.util.Log
import ke.ac.ku.ledgerly.data.datasource.CurrencyDataSource
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CurrencyManager @Inject constructor(
    private val currencyDataSource: CurrencyDataSource,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    companion object {
        private const val TAG = "CurrencyManager"
        private const val BASE_CURRENCY = "USD"
        private const val DEFAULT_CURRENCY = "USD"
    }

     suspend fun getDisplayCurrency(): String {
        return try {
            userPreferencesRepository.currency.first().takeIf { it.isNotEmpty() } ?: DEFAULT_CURRENCY
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get display currency, using default", e)
            DEFAULT_CURRENCY
        }
    }

      fun getDisplayCurrencyFlow(): Flow<String> {
        return userPreferencesRepository.currency
            .catch { e ->
                Log.e(TAG, "Error getting display currency flow", e)
                emit(DEFAULT_CURRENCY)
            }
    }

   suspend fun convertToDisplayCurrency(
        amountUsd: BigDecimal,
        targetCurrency: String? = null
    ): String {
        val displayCurrency = targetCurrency ?: getDisplayCurrency()
        
        if (displayCurrency == BASE_CURRENCY) {
            return amountUsd.setScale(2, RoundingMode.HALF_UP).toString()
        }

        return try {
            val rate = getExchangeRate(BASE_CURRENCY, displayCurrency)
            amountUsd.multiply(rate)
                .setScale(2, RoundingMode.HALF_UP).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert USD to $displayCurrency", e)
            // Return original amount if conversion fails
            amountUsd.toString()
        }
    }


    suspend fun convertToUsd(
        amount: BigDecimal,
        sourceCurrency: String
    ): BigDecimal {
        if (sourceCurrency == BASE_CURRENCY) {
            return amount
        }

        return try {
            val rate = getExchangeRate(sourceCurrency, BASE_CURRENCY)
            amount.multiply(rate)
                .setScale(2, RoundingMode.HALF_UP)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert $sourceCurrency to USD", e)
            throw IllegalStateException("Cannot obtain exchange rate for $sourceCurrency to $BASE_CURRENCY", e)
        }
    }

     suspend fun getFrozenExchangeRate(currency: String): BigDecimal {
        if (currency == BASE_CURRENCY) {
            return BigDecimal.ONE
        }

        return try {
            getExchangeRate(currency, BASE_CURRENCY)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get frozen exchange rate for $currency", e)
            throw IllegalStateException("Cannot obtain exchange rate for $currency", e)
        }
    }

       private suspend fun getExchangeRate(
        fromCurrency: String,
        toCurrency: String
    ): BigDecimal {
        if (fromCurrency == toCurrency) {
            return BigDecimal.ONE
        }

        return try {
            val rates = currencyDataSource.getExchangeRates(fromCurrency).rates
            val rate = rates[toCurrency]
                ?: throw IllegalStateException("Exchange rate for $toCurrency not found in response for base $fromCurrency")
            BigDecimal.valueOf(rate)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching exchange rate from $fromCurrency to $toCurrency", e)
            throw e
        }
    }

        suspend fun sumAndConvert(
        amounts: List<BigDecimal>,
        targetCurrency: String? = null
    ): BigDecimal {
        val sumUsd = amounts.fold(BigDecimal.ZERO) { acc, amount ->
            acc.add(amount)
        }
        return convertToDisplayCurrency(sumUsd, targetCurrency).toBigDecimal()
    }
    suspend fun refreshExchangeRates() {
        try {
            currencyDataSource.getExchangeRates(BASE_CURRENCY)
            Log.d(TAG, "Exchange rates refreshed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh exchange rates", e)
            // Fail silently - will use cached rates
        }
    }

    suspend fun isOfflineMode(): Boolean {
        return try {
                    currencyDataSource.getExchangeRates(BASE_CURRENCY)
            false
        } catch (e: Exception) {

            true
        }
    }
}
