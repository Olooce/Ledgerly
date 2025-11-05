package ke.ac.ku.ledgerly.ui.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private const val TIMEOUT_MILLIS = 5000L
    }

    val isDarkMode: StateFlow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = false
        )

    fun toggleTheme() {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    val currentValue = preferences[DARK_MODE_KEY] ?: false
                    preferences[DARK_MODE_KEY] = !currentValue
                }
            } catch (e: IOException) {
                // silently fail to prevent crashes
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[DARK_MODE_KEY] = enabled
                }
            } catch (e: IOException) {
            }
        }
    }
}