package ke.ac.ku.ledgerly.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _budgets = MutableStateFlow<List<BudgetEntity>>(emptyList())
    val budgets: StateFlow<List<BudgetEntity>> = _budgets.asStateFlow()

    private val _alerts = MutableStateFlow<List<BudgetEntity>>(emptyList())
    val alerts: StateFlow<List<BudgetEntity>> = _alerts.asStateFlow()

    private val _uiState = MutableStateFlow<BudgetUiState>(BudgetUiState.Loading)
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
        loadAlerts()
    }

    fun loadBudgets() {
        viewModelScope.launch {
            _uiState.value = BudgetUiState.Loading
            try {
                budgetRepository.refreshBudgetSpending()
                _budgets.value = budgetRepository.getBudgetsForCurrentMonth()
                _uiState.value = BudgetUiState.Success
            } catch (e: Exception) {
                _uiState.value = BudgetUiState.Error(e.message ?: "Failed to load budgets")
            }
        }
    }

    fun loadAlerts() {
        viewModelScope.launch {
            _alerts.value = budgetRepository.getBudgetsExceedingThreshold(80)
        }
    }

    fun setBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            try {
                budgetRepository.setBudget(budget)
                loadBudgets()
                loadAlerts()
            } catch (e: Exception) {
                _uiState.value = BudgetUiState.Error("Failed to set budget: ${e.message}")
            }
        }
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(category)
                loadBudgets()
                loadAlerts()
            } catch (e: Exception) {
                _uiState.value = BudgetUiState.Error("Failed to delete budget: ${e.message}")
            }
        }
    }
}

sealed class BudgetUiState {
    object Loading : BudgetUiState()
    object Success : BudgetUiState()
    data class Error(val message: String) : BudgetUiState()
}