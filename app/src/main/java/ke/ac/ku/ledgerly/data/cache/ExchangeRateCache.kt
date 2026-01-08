
package ke.ac.ku.ledgerly.data.cache

import ke.ac.ku.ledgerly.data.model.ExchangeRateResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRateCache @Inject constructor() {
    private val cache = mutableMapOf<String, Pair<Long, ExchangeRateResponse>>()

    fun getRates(baseCurrency: String): ExchangeRateResponse? {
        return cache[baseCurrency]?.second
    }

    fun saveRates(baseCurrency: String, rates: ExchangeRateResponse) {
        cache[baseCurrency] = System.currentTimeMillis() to rates
    }

    fun isStale(baseCurrency: String): Boolean {
        val lastUpdated = cache[baseCurrency]?.first ?: return true
        val twentyFourHoursInMillis = 24 * 60 * 60 * 1000
        return (System.currentTimeMillis() - lastUpdated) > twentyFourHoursInMillis
    }
}
