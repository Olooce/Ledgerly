package ke.ac.ku.ledgerly.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.NavigationEvent
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val userName: String = "",
    val currency: String = "KES",
    val monthlyBudget: String = "0",
    val notificationEnabled: Boolean = true,
    val darkMode: Boolean = false,
    val isEditing: Boolean = false,
    val editedUserName: String = "",
    val editedCurrency: String = "KES",
    val editedMonthlyBudget: String = "0",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

sealed class ProfileUiEvent {
    object OnBackClicked : ProfileUiEvent()
    object OnEditClicked : ProfileUiEvent()
    object OnSaveClicked : ProfileUiEvent()
    object OnCancelClicked : ProfileUiEvent()
    data class OnUserNameChanged(val newName: String) : ProfileUiEvent()
    data class OnCurrencyChanged(val newCurrency: String) : ProfileUiEvent()
    data class OnMonthlyBudgetChanged(val newBudget: String) : ProfileUiEvent()
    data class OnNotificationToggled(val enabled: Boolean) : ProfileUiEvent()
    data class OnDarkModeToggled(val enabled: Boolean) : ProfileUiEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    val userName: StateFlow<String> = userPreferencesRepository.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val currency: StateFlow<String> = userPreferencesRepository.currency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "KES"
        )

    val monthlyBudget: StateFlow<String> = userPreferencesRepository.monthlyBudget
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "0"
        )

    val notificationEnabled: StateFlow<Boolean> = userPreferencesRepository.notificationEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val darkMode: StateFlow<Boolean> = userPreferencesRepository.darkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            try {
                userName.collect { name ->
                    _profileState.update { it.copy(userName = name, editedUserName = name) }
                }
            } catch (e: Exception) {
                _profileState.update { it.copy(error = "Failed to load profile data") }
            }
        }

        viewModelScope.launch {
            currency.collect { curr ->
                _profileState.update { it.copy(currency = curr, editedCurrency = curr) }
            }
        }

        viewModelScope.launch {
            monthlyBudget.collect { budget ->
                _profileState.update {
                    it.copy(
                        monthlyBudget = budget,
                        editedMonthlyBudget = budget
                    )
                }
            }
        }

        viewModelScope.launch {
            notificationEnabled.collect { enabled ->
                _profileState.update { it.copy(notificationEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            darkMode.collect { dark ->
                _profileState.update { it.copy(darkMode = dark) }
            }
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            ProfileUiEvent.OnBackClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                }
            }

            ProfileUiEvent.OnEditClicked -> {
                _profileState.update { it.copy(isEditing = true) }
            }

            ProfileUiEvent.OnCancelClicked -> {
                _profileState.update {
                    it.copy(
                        isEditing = false,
                        editedUserName = it.userName,
                        editedCurrency = it.currency,
                        editedMonthlyBudget = it.monthlyBudget
                    )
                }
            }

            ProfileUiEvent.OnSaveClicked -> {
                saveProfileChanges()
            }

            is ProfileUiEvent.OnUserNameChanged -> {
                _profileState.update { it.copy(editedUserName = event.newName) }
            }

            is ProfileUiEvent.OnCurrencyChanged -> {
                _profileState.update { it.copy(editedCurrency = event.newCurrency) }
            }

            is ProfileUiEvent.OnMonthlyBudgetChanged -> {
                _profileState.update { it.copy(editedMonthlyBudget = event.newBudget) }
            }

            is ProfileUiEvent.OnNotificationToggled -> {
                toggleNotifications(event.enabled)
            }

            is ProfileUiEvent.OnDarkModeToggled -> {
                toggleDarkMode(event.enabled)
            }
        }
    }

    private fun saveProfileChanges() {
        val state = _profileState.value

        viewModelScope.launch {
            try {
                _profileState.update { it.copy(isLoading = true) }

                // Save all changes
                if (state.editedUserName != state.userName) {
                    userPreferencesRepository.saveUserName(state.editedUserName)
                }

                if (state.editedCurrency != state.currency) {
                    userPreferencesRepository.saveCurrency(state.editedCurrency)
                }

                if (state.editedMonthlyBudget != state.monthlyBudget) {
                    userPreferencesRepository.saveMonthlyBudget(state.editedMonthlyBudget)
                }

                _profileState.update {
                    it.copy(
                        isEditing = false,
                        isLoading = false,
                        successMessage = "Profile updated successfully",
                        userName = state.editedUserName,
                        currency = state.editedCurrency,
                        monthlyBudget = state.editedMonthlyBudget
                    )
                }

                // Clear success message after 2 seconds
                kotlinx.coroutines.delay(2000)
                _profileState.update { it.copy(successMessage = null) }
            } catch (e: Exception) {
                _profileState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to save profile changes: ${e.message}"
                    )
                }
            }
        }
    }

    private fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.saveNotificationEnabled(enabled)
                _profileState.update { it.copy(notificationEnabled = enabled) }
            } catch (e: Exception) {
                _profileState.update { it.copy(error = "Failed to update notification settings") }
            }
        }
    }

    private fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.saveDarkMode(enabled)
                _profileState.update { it.copy(darkMode = enabled) }
            } catch (e: Exception) {
                _profileState.update { it.copy(error = "Failed to update dark mode") }
            }
        }
    }
}
