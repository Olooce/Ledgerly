package ke.ac.ku.ledgerly.service

import android.util.Log
import ke.ac.ku.ledgerly.data.api.ExchangeRateApi
import ke.ac.ku.ledgerly.data.cache.ExchangeRateCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRateService @Inject constructor(
    private val cache: ExchangeRateCache,
    private val api: ExchangeRateApi
) {
    private val TAG = "ExchangeRateService"


    suspend fun getRates(base: String = "USD"): Map<String, Double>? {

        return try {
            val cached = cache.getRates(base)
            if (cached != null && !cache.isStale(base)) {
                Log.i(TAG, "Using cached rates for $base")
                return cached.rates
            }

            Log.i(TAG, "Fetching rates from API for base: $base")
            val response = api.getRates(base)
            cache.saveRates(base, response)
            response.rates
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching rates: ${e.message}", e)
            cache.getRates(base)?.rates
        }
    }

}