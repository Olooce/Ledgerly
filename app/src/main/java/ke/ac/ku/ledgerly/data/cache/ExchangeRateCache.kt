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
        private val BASE_KEY = stringPreferencesKey("base_code")
        private val LAST_FETCH_KEY = longPreferencesKey("last_fetch_time")
    }

    suspend fun saveRates(base: String, rates: Map<String, Double>) {
        context.dataStore.edit { prefs ->
            prefs[RATES_KEY] = Gson().toJson(rates)
            prefs[BASE_KEY] = base
            prefs[LAST_FETCH_KEY] = System.currentTimeMillis()
            Log.i("ExchangeRateCache", "Saved $base rates: ${rates.size} entries")
        }
    }

    suspend fun getRates(base: String): Map<String, Double>? {
        val prefs = context.dataStore.data.first()
        val cachedBase = prefs[BASE_KEY]
        if (cachedBase != base) return null

        val json = prefs[RATES_KEY] ?: return null
        return try {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            Gson().fromJson<Map<String, Double>>(json, type)
        } catch (e: Exception) {
            context.dataStore.edit { it.remove(RATES_KEY); it.remove(BASE_KEY); it.remove(LAST_FETCH_KEY) }
            null
        }
    }

    suspend fun isCacheValid(): Boolean {
        val prefs = context.dataStore.data.first()
        val lastFetch = prefs[LAST_FETCH_KEY] ?: return false
        return System.currentTimeMillis() - lastFetch < TimeUnit.DAYS.toMillis(1)
    }
}

