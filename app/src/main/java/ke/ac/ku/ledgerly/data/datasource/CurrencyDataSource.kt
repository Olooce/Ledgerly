package ke.ac.ku.ledgerly.data.datasource

import ke.ac.ku.ledgerly.data.api.ExchangeRateApi
import ke.ac.ku.ledgerly.data.cache.ExchangeRateCache
import ke.ac.ku.ledgerly.data.model.ExchangeRateResponse
import javax.inject.Inject

class CurrencyDataSource @Inject constructor(
    private val exchangeRateApi: ExchangeRateApi,
    private val exchangeRateCache: ExchangeRateCache
) {

    suspend fun getExchangeRates(baseCurrency: String): ExchangeRateResponse {
        val cachedRates = exchangeRateCache.getRates(baseCurrency)
        return if (cachedRates != null && !exchangeRateCache.isStale(baseCurrency)) {
            cachedRates
        } else {
            val remoteRates = exchangeRateApi.getRates(baseCurrency)
            exchangeRateCache.saveRates(baseCurrency, remoteRates)
            remoteRates
        }
    }
}
