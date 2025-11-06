package ke.ac.ku.ledgerly.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentStep: Int = 0,
    val userName: String = "",
    val currency: String = "KES",
    val monthlyBudget: String = "",
    val notificationEnabled: Boolean = true,
    val canProceed: Boolean = true,
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun updateUserName(name: String) {
        _state.update { it.copy(userName = name) }
        updateCanProceed()
    }

    fun updateCurrency(currency: String) {
        _state.update { it.copy(currency = currency) }
    }

    fun updateMonthlyBudget(budget: String) {
        _state.update { it.copy(monthlyBudget = budget) }
    }

    fun toggleNotification() {
        _state.update { it.copy(notificationEnabled = !_state.value.notificationEnabled) }
    }

    fun nextStep() {
        val currentState = _state.value

        if (currentState.currentStep < 3) {
            _state.update { it.copy(currentStep = it.currentStep + 1) }
            updateCanProceed()
        } else {
            completeOnboarding()
        }
    }

    fun previousStep() {
        _state.update {
            if (it.currentStep > 0) it.copy(currentStep = it.currentStep - 1) else it
        }
        updateCanProceed()
    }

    private fun updateCanProceed() {
        val currentState = _state.value
        val canProceed = when (currentState.currentStep) {
            1 -> currentState.userName.isNotBlank()
            2 -> currentState.currency.isNotBlank()
            else -> true
        }
        _state.update { it.copy(canProceed = canProceed) }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            val current = _state.value

            userPreferencesRepository.batchSave {
                saveUserName(current.userName, syncNow = false)
                saveCurrency(current.currency, syncNow = false)
                saveMonthlyBudget(current.monthlyBudget, syncNow = false)
                saveNotificationEnabled(current.notificationEnabled, syncNow = false)
                completeOnboarding(syncNow = false)
            }

            _state.update { it.copy(isCompleted = true) }
        }
    }
}
