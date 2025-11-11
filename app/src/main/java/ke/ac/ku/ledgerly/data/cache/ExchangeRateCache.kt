package ke.ac.ku.ledgerly.data.cache

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore("exchange_rate_cache")

class ExchangeRateCache(private val context: Context) {
    companion object {
        private val RATES_KEY = stringPreferencesKey("rates_json")
        private val LAST_FETCH_KEY = longPreferencesKey("last_fetch_time")
    }

    suspend fun saveRates(rates: Map<String, Double>) {
        context.dataStore.edit { prefs ->
            val json = Gson().toJson(rates)
            Log.i("ExchangeRateCache", "Saving rates: ${rates.size} entries")
            prefs[RATES_KEY] = json
            prefs[LAST_FETCH_KEY] = System.currentTimeMillis()
        }
    }


    suspend fun getRates(): Map<String, Double>? {
        val prefs = context.dataStore.data.first()
        val json = prefs[RATES_KEY] ?: return null

        return try {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            val map: Map<String, Double> = Gson().fromJson(json, type)
            Log.i("ExchangeRateCache", "Successfully loaded cached rates: ${map.size}")
            map
        } catch (e: Exception) {
            Log.e("ExchangeRateCache", "Failed to parse cached rates, clearing cache", e)
            context.dataStore.edit { it.remove(RATES_KEY); it.remove(LAST_FETCH_KEY) }
            return null
        }
    }

    suspend fun isCacheValid(): Boolean {
        val prefs = context.dataStore.data.first()
        val lastFetch = prefs[LAST_FETCH_KEY] ?: return false
        val elapsed = System.currentTimeMillis() - lastFetch
        return elapsed < TimeUnit.DAYS.toMillis(1)
    }
}
