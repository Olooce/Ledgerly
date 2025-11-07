package ke.ac.ku.ledgerly.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import ke.ac.ku.ledgerly.domain.SyncManager
import ke.ac.ku.ledgerly.ui.theme.ThemeViewModel

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _isSyncEnabled = MutableStateFlow(false)
    val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(true)
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.syncEnabled.collect { _isSyncEnabled.value = it }
        }

        viewModelScope.launch {
            userPreferencesRepository.notificationEnabled.collect { _isNotificationEnabled.value = it }
        }
    }

    fun toggleCloudSync(enabled: Boolean) {
        viewModelScope.launch {
            _isSyncEnabled.value = enabled

            val result = userPreferencesRepository.saveSyncEnabled(enabled, syncNow = false)

            if (result.isSuccess) {
                // Only sync data when enabling
                if (enabled) {
                    userPreferencesRepository.syncToFirestore()
                }

                syncManager.setCloudSyncEnabled(enabled)
            } else {
                _errorMessage.value = "Failed to update sync settings"
                _isSyncEnabled.value = !enabled
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
