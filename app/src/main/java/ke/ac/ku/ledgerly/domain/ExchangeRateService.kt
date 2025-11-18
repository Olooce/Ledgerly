package ke.ac.ku.ledgerly.domain

import android.content.Context
import android.util.Log
import ke.ac.ku.ledgerly.data.api.ExchangeRateApi
import ke.ac.ku.ledgerly.data.cache.ExchangeRateCache
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Inet4Address
import java.net.InetAddress


/*
 * Example JSON response from ExchangeRate-API
 *
 * {
 *   "result": "success",
 *   "provider": "https://www.exchangerate-api.com",
 *   "documentation": "https://www.exchangerate-api.com/docs/free",
 *   "terms_of_use": "https://www.exchangerate-api.com/terms",
 *   "time_last_update_unix": 1585872397,
 *   "time_last_update_utc": "Fri, 02 Apr 2020 00:06:37 +0000",
 *   "time_next_update_unix": 1585959987,
 *   "time_next_update_utc": "Sat, 03 Apr 2020 00:26:27 +0000",
 *   "time_eol_unix": 0,
 *   "base_code": "USD",
 *   "rates": {
 *     "USD": 1,
 *     "AED": 3.67,
 *     "ARS": 64.51,
 *     "AUD": 1.65,
 *     "CAD": 1.42,
 *     "CHF": 0.97,
 *     "CLP": 864.53,
 *     "CNY": 7.1,
 *     "EUR": 0.919,
 *     "GBP": 0.806,
 *     "HKD": 7.75,
 *     ...
 *   }
 * }
 */

object ExchangeRateService {
    private const val TAG = "ExchangeRateService"
    private const val BASE_URL = "https://open.er-api.com/"

    private val api: ExchangeRateApi by lazy {
        val logger = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val ipv4Dns = object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                val all = Dns.SYSTEM.lookup(hostname)
                return all.filter { it is Inet4Address }
            }
        }

        val client = OkHttpClient.Builder()
            .dns(ipv4Dns)
            .addInterceptor(logger)
            .build()


        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ExchangeRateApi::class.java)
    }

    suspend fun getRates(context: Context, base: String = "USD"): Map<String, Double>? {
        val cache = ExchangeRateCache(context)

        return try {
            val cached = cache.getRates(base)
            if (cached != null && cache.isCacheValid()) {
                Log.i(TAG, "Using cached rates for $base")
                return cached
            }

            Log.i(TAG, "Fetching rates from API for base: $base")
            val response = api.getRates(base)
            cache.saveRates(base, response.rates)
            response.rates
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching rates: ${e.message}", e)
            cache.getRates(base)
        }
    }

}
