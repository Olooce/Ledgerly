package ke.ac.ku.ledgerly.presentation.savings_goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.data.model.SavingsGoalEntity
import ke.ac.ku.ledgerly.data.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavingsGoalViewModel @Inject constructor(
    private val repository: SavingsGoalRepository
) : ViewModel() {

    private val _allGoals = MutableStateFlow<List<SavingsGoalEntity>>(emptyList())
    val allGoals: StateFlow<List<SavingsGoalEntity>> = _allGoals.asStateFlow()

    private val _activeGoals = MutableStateFlow<List<SavingsGoalEntity>>(emptyList())
    val activeGoals: StateFlow<List<SavingsGoalEntity>> = _activeGoals.asStateFlow()

    private val _completedGoals = MutableStateFlow<List<SavingsGoalEntity>>(emptyList())
    val completedGoals: StateFlow<List<SavingsGoalEntity>> = _completedGoals.asStateFlow()

    private val _selectedGoal = MutableStateFlow<SavingsGoalEntity?>(null)
    val selectedGoal: StateFlow<SavingsGoalEntity?> = _selectedGoal.asStateFlow()

    private val _totalSavings = MutableStateFlow(0.0)
    val totalSavings: StateFlow<Double> = _totalSavings.asStateFlow()

    private val _totalTarget = MutableStateFlow(0.0)
    val totalTarget: StateFlow<Double> = _totalTarget.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadAllGoals()
    }

    private fun loadAllGoals() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getAllGoals().collect { goals ->
                    _allGoals.value = goals
                    _activeGoals.value = goals.filter { !it.isCompleted }
                    _completedGoals.value = goals.filter { it.isCompleted }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSavingsSummary() {
        viewModelScope.launch {
            try {
                repository.getSavingsSummary().collect { summary ->
                    _totalSavings.value = summary.totalSaved ?: 0.0
                    _totalTarget.value = summary.totalTarget ?: 0.0
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun selectGoal(id: Long) {
        viewModelScope.launch {
            try {
                repository.getGoalById(id).collect { goal ->
                    _selectedGoal.value = goal
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun addGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.insertGoal(goal)
                loadAllGoals()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add goal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateGoal(goal)
                loadAllGoals()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update goal: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGoalProgress(goalId: Long, newAmount: Double) {
        viewModelScope.launch {
            try {
                repository.updateGoalAmount(goalId, newAmount)
                loadAllGoals()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update progress: ${e.message}"
            }
        }
    }

    fun completeGoal(goalId: Long) {
        viewModelScope.launch {
            try {
                repository.completeGoal(goalId)
                loadAllGoals()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to complete goal: ${e.message}"
            }
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteGoal(goalId)
                loadAllGoals()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete goal: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
