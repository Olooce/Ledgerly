package ke.ac.ku.ledgerly.data.cache

import ke.ac.ku.ledgerly.data.dao.ExchangeRateDao
import ke.ac.ku.ledgerly.data.model.ExchangeRateEntity
import ke.ac.ku.ledgerly.data.model.ExchangeRateResponse
import ke.ac.ku.ledgerly.utils.toExchangeRateResponse
import ke.ac.ku.ledgerly.utils.toJson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRateCache @Inject constructor(
    private val dao: ExchangeRateDao
) {
    private val memoryCache = mutableMapOf<String, Pair<Long, ExchangeRateResponse>>()

    private val TTL = 24 * 60 * 60 * 1000L // 24 hours

    suspend fun getRates(baseCurrency: String): ExchangeRateResponse? {
        memoryCache[baseCurrency]?.let { (timestamp, rates) ->
            if (!isStale(timestamp)) {
                return rates
            }
        }

        val cachedEntity = dao.getRates(baseCurrency)
        if (cachedEntity != null && !isStale(cachedEntity.lastUpdated)) {
            val rates = cachedEntity.ratesJson.toExchangeRateResponse()
            memoryCache[baseCurrency] = cachedEntity.lastUpdated to rates
            return rates
        }

        return null
    }

    suspend fun saveRates(baseCurrency: String, rates: ExchangeRateResponse) {
        val timestamp = System.currentTimeMillis()

        memoryCache[baseCurrency] = timestamp to rates

        dao.saveRates(
            ExchangeRateEntity(
                baseCurrency = baseCurrency,
                ratesJson = rates.toJson(),
                lastUpdated = timestamp
            )
        )
    }

    fun isStale(baseCurrency: String): Boolean {
        val timestamp = memoryCache[baseCurrency]?.first ?: return true
        return isStale(timestamp)
    }

    private fun isStale(timestamp: Long): Boolean {
        return (System.currentTimeMillis() - timestamp) > TTL
    }

    suspend fun clearAll() {
        memoryCache.clear()
        dao.clearAll()
    }
}