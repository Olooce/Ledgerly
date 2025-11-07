package ke.ac.ku.ledgerly.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import ke.ac.ku.ledgerly.domain.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _isSyncEnabled = MutableStateFlow(false)
    val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(true)
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.syncEnabled.collect { enabled ->
                _isSyncEnabled.value = enabled
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.darkMode.collect { enabled ->
                _isDarkMode.value = enabled
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.notificationEnabled.collect { enabled ->
                _isNotificationEnabled.value = enabled
            }
        }
    }

    fun toggleCloudSync(enabled: Boolean) {
        viewModelScope.launch {
            _isSyncEnabled.value = enabled

            val result = userPreferencesRepository.saveSyncEnabled(enabled, syncNow = false)

            if (result.isSuccess) {
                syncManager.setCloudSyncEnabled(enabled)

                if (enabled) {
                    userPreferencesRepository.syncToFirestore()
                }
            } else {
                _errorMessage.value = "Failed to update sync settings"
                _isSyncEnabled.value = !enabled
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _isDarkMode.value = enabled

            val result = userPreferencesRepository.saveDarkMode(enabled)

            if (result.isFailure) {
                _errorMessage.value = "Failed to update dark mode setting"
                _isDarkMode.value = !enabled
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _isNotificationEnabled.value = enabled

            val result = userPreferencesRepository.saveNotificationEnabled(enabled)

            if (result.isFailure) {
                _errorMessage.value = "Failed to update notification setting"
                _isNotificationEnabled.value = !enabled
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}