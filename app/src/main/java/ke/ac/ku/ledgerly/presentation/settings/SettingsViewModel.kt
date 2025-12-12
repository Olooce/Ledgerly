package ke.ac.ku.ledgerly.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.WorkManagerSetup
import ke.ac.ku.ledgerly.data.repository.AuthRepository
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import ke.ac.ku.ledgerly.domain.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val syncManager: SyncManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManagerSetup: WorkManagerSetup,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isSyncEnabled = MutableStateFlow(false)
    val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(true)
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    private val _syncWifiOnly = MutableStateFlow(true)
    val syncWifiOnly: StateFlow<Boolean> = _syncWifiOnly.asStateFlow()

    private val _syncChargingOnly = MutableStateFlow(false)
    val syncChargingOnly: StateFlow<Boolean> = _syncChargingOnly.asStateFlow()

    private val _syncInterval = MutableStateFlow(6L) // hours
    val syncInterval: StateFlow<Long> = _syncInterval.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isBiometricAvailable = MutableStateFlow(false)
    val isBiometricAvailable: StateFlow<Boolean> = _isBiometricAvailable.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(false)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isSessionTimeoutEnabled = MutableStateFlow(false)
    val isSessionTimeoutEnabled: StateFlow<Boolean> = _isSessionTimeoutEnabled.asStateFlow()

    private val _sessionTimeoutMinutes = MutableStateFlow(15L)
    val sessionTimeoutMinutes: StateFlow<Long> = _sessionTimeoutMinutes.asStateFlow()

    init {
        loadPreferences()
        checkBiometricAvailability()
        checkBiometricEnabled()
    }

    private fun checkBiometricAvailability() {
        _isBiometricAvailable.value = authRepository.isBiometricAvailable()
    }

    private fun checkBiometricEnabled() {
        _isBiometricEnabled.value = authRepository.isBiometricUnlockEnabled()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.syncEnabled.collect {
                _isSyncEnabled.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.notificationEnabled.collect {
                _isNotificationEnabled.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.syncWifiOnly.collect {
                _syncWifiOnly.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.syncChargingOnly.collect {
                _syncChargingOnly.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.syncInterval.collect {
                _syncInterval.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.sessionTimeoutEnabled.collect {
                _isSessionTimeoutEnabled.value = it
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.sessionTimeoutMinutes.collect {
                _sessionTimeoutMinutes.value = it
            }
        }
    }


    fun toggleCloudSync(enabled: Boolean) {
        viewModelScope.launch {
            _isSyncEnabled.value = enabled
            val result = userPreferencesRepository.saveSyncEnabled(enabled, syncNow = false)

            if (result.isSuccess) {
                val remoteResult = userPreferencesRepository.syncToFirestore()
                if (remoteResult.isFailure) {
                    _errorMessage.value = "Failed to sync preference to Firestore"
                }

                if (enabled) {
                    schedulePeriodicSync()
                } else {
                    workManagerSetup.cancelPeriodicSync()
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

    fun toggleBiometricUnlock(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        if (enabled) {
            authRepository.enableBiometricUnlock()
        } else {
            authRepository.disableBiometricUnlock()
        }
    }

    fun toggleSessionTimeout(enabled: Boolean) {
        viewModelScope.launch {
            _isSessionTimeoutEnabled.value = enabled
            val result = userPreferencesRepository.saveSessionTimeoutEnabled(enabled)
            if (result.isFailure) {
                _errorMessage.value = "Failed to update session timeout setting"
                _isSessionTimeoutEnabled.value = !enabled
            }
        }
    }

    fun updateSessionTimeoutMinutes(minutes: Long) {
        viewModelScope.launch {
            _sessionTimeoutMinutes.value = minutes
            val result = userPreferencesRepository.saveSessionTimeoutMinutes(minutes)
            if (result.isFailure) {
                _errorMessage.value = "Failed to update session timeout duration"
            }
        }
    }

    fun updateSyncWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            _syncWifiOnly.value = wifiOnly
            val result = userPreferencesRepository.saveSyncWifiOnly(wifiOnly)
            if (result.isSuccess) {
                if (_isSyncEnabled.value) {
                    schedulePeriodicSync()
                }
            } else {
                _errorMessage.value = "Failed to update WiFi setting"
                _syncWifiOnly.value = !wifiOnly
            }
        }
    }

    fun updateSyncChargingOnly(chargingOnly: Boolean) {
        viewModelScope.launch {
            _syncChargingOnly.value = chargingOnly
            val result = userPreferencesRepository.saveSyncChargingOnly(chargingOnly)
            if (result.isSuccess) {
                if (_isSyncEnabled.value) {
                    schedulePeriodicSync()
                }
            } else {
                _errorMessage.value = "Failed to update charging setting"
                _syncChargingOnly.value = !chargingOnly
            }
        }
    }

    fun updateSyncInterval(hours: Long) {
        viewModelScope.launch {
            _syncInterval.value = hours
            val result = userPreferencesRepository.saveSyncInterval(hours)
            if (result.isSuccess) {
                if (_isSyncEnabled.value) {
                    schedulePeriodicSync()
                }
            } else {
                _errorMessage.value = "Failed to update sync interval"
            }
        }
    }

    fun manualSync() {
        if (_isSyncing.value) {
            _errorMessage.value = "Sync already in progress"
            return
        }
        viewModelScope.launch {
            _isSyncing.value = true
            _errorMessage.value = null
            syncManager.syncAllData(
                onSuccess = {
                    _errorMessage.value = "Sync completed successfully"
                    _isSyncing.value = false
                },
                onError = { error ->
                    _errorMessage.value = "Sync failed: $error"
                    _isSyncing.value = false
                }
            )
        }
    }

    private fun schedulePeriodicSync() {
        workManagerSetup.schedulePeriodicSync(
            repeatIntervalHours = _syncInterval.value,
            requireWifi = _syncWifiOnly.value,
            requireCharging = _syncChargingOnly.value
        )
    }

    fun clearError() {
        _errorMessage.value = null
    }
}