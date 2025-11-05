package ke.ac.ku.ledgerly.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.domain.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {

    private val _isSyncEnabled = MutableStateFlow(syncManager.isCloudSyncEnabled())
    val isSyncEnabled: StateFlow<Boolean> = _isSyncEnabled

    fun toggleCloudSync(enabled: Boolean) {
        _isSyncEnabled.value = enabled
        viewModelScope.launch {
            syncManager.setCloudSyncEnabled(enabled)
        }
    }
}
