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
import kotlinx.coroutines.flow.combine
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

      private val uiState = MutableStateFlow(
        ProfileState()
    )

    private val repoProfileState: StateFlow<ProfileState> =
        combine(
            userPreferencesRepository.userName,
            userPreferencesRepository.currency,
            userPreferencesRepository.monthlyBudget,
            userPreferencesRepository.notificationEnabled,
            userPreferencesRepository.darkMode
        ) { name, currency, budget, notifications, darkMode ->
            ProfileState(
                userName = name,
                currency = currency,
                monthlyBudget = budget,
                notificationEnabled = notifications,
                darkMode = darkMode
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileState()
        )

    val profileState: StateFlow<ProfileState> =
        combine(repoProfileState, uiState) { repo, ui ->
            ui.copy(
                userName = repo.userName,
                currency = repo.currency,
                monthlyBudget = repo.monthlyBudget,
                notificationEnabled = repo.notificationEnabled,
                darkMode = repo.darkMode,
                editedUserName = if (ui.isEditing) ui.editedUserName else repo.userName,
                editedCurrency = if (ui.isEditing) ui.editedCurrency else repo.currency,
                editedMonthlyBudget = if (ui.isEditing) ui.editedMonthlyBudget else repo.monthlyBudget
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileState()
        )

    fun onEvent(event: ProfileUiEvent) {
        when (event) {

            ProfileUiEvent.OnBackClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                }
            }

            ProfileUiEvent.OnEditClicked -> {
                val state = profileState.value

                uiState.update {
                    it.copy(
                        isEditing = true,
                        editedUserName = state.userName,
                        editedCurrency = state.currency,
                        editedMonthlyBudget = state.monthlyBudget,
                        error = null
                    )
                }
            }


            ProfileUiEvent.OnCancelClicked -> {
                val state = profileState.value

                uiState.update {
                    it.copy(
                        isEditing = false,
                        editedUserName = state.userName,
                        editedCurrency = state.currency,
                        editedMonthlyBudget = state.monthlyBudget,
                        error = null
                    )
                }
            }


            ProfileUiEvent.OnSaveClicked -> saveProfileChanges()

            is ProfileUiEvent.OnUserNameChanged ->
                uiState.update { it.copy(editedUserName = event.newName) }

            is ProfileUiEvent.OnCurrencyChanged ->
                uiState.update { it.copy(editedCurrency = event.newCurrency) }

            is ProfileUiEvent.OnMonthlyBudgetChanged ->
                uiState.update { it.copy(editedMonthlyBudget = event.newBudget) }

            is ProfileUiEvent.OnNotificationToggled ->
                toggleNotifications(event.enabled)

            is ProfileUiEvent.OnDarkModeToggled ->
                toggleDarkMode(event.enabled)
        }
    }

    private fun saveProfileChanges() {
        val state = profileState.value

        viewModelScope.launch {
            try {
                uiState.update { it.copy(isLoading = true) }

                if (state.editedUserName != state.userName) {
                    userPreferencesRepository.saveUserName(state.editedUserName)
                }

                if (state.editedCurrency != state.currency) {
                    userPreferencesRepository.saveCurrency(state.editedCurrency)
                }

                if (state.editedMonthlyBudget != state.monthlyBudget) {
                    userPreferencesRepository.saveMonthlyBudget(state.editedMonthlyBudget)
                }

                uiState.update {
                    it.copy(
                        isEditing = false,
                        isLoading = false,
                        successMessage = "Profile updated successfully"
                    )
                }

                kotlinx.coroutines.delay(2_000)
                uiState.update { it.copy(successMessage = null) }

            } catch (e: Exception) {
                uiState.update {
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
            } catch (e: Exception) {
                uiState.update { it.copy(error = "Failed to update notification settings") }
            }
        }
    }

    private fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.saveDarkMode(enabled)
            } catch (e: Exception) {
                uiState.update { it.copy(error = "Failed to update dark mode") }
            }
        }
    }
}

