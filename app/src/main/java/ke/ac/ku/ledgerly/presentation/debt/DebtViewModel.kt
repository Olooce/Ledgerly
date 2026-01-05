package ke.ac.ku.ledgerly.presentation.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.NavigationEvent
import ke.ac.ku.ledgerly.data.model.DebtEntity
import ke.ac.ku.ledgerly.data.repository.DebtRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DebtListState(
    val debts: List<DebtEntity> = emptyList(),
    val overdueDebts: List<DebtEntity> = emptyList(),
    val upcomingDebts: List<DebtEntity> = emptyList(),
    val totalOwed: Double = 0.0,
    val totalOwe: Double = 0.0,
    val overdueCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: String = "all" // all, owe, owed, overdue, upcoming
)

data class DebtDetailState(
    val debt: DebtEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AddEditDebtState(
    val personName: String = "",
    val amount: String = "",
    val debtType: String = "owe", // owe or owed
    val dueDate: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // 7 days from now
    val status: String = "pending",
    val description: String = "",
    val reminderDays: String = "0",
    val reminderEnabled: Boolean = true,
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false,
    val editingDebtId: Long? = null
)

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val debtRepository: DebtRepository
) : ViewModel() {

    private val _debtListState = MutableStateFlow(DebtListState())
    val debtListState: StateFlow<DebtListState> = _debtListState.asStateFlow()

    private val _debtDetailState = MutableStateFlow(DebtDetailState())
    val debtDetailState: StateFlow<DebtDetailState> = _debtDetailState.asStateFlow()

    private val _addEditState = MutableStateFlow(AddEditDebtState())
    val addEditState: StateFlow<AddEditDebtState> = _addEditState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        loadDebts()
    }

    fun loadDebts() {
        viewModelScope.launch {
            _debtListState.update { it.copy(isLoading = true) }
            try {
                debtRepository.getAllDebts().collect { debts ->
                    val overdueDebts = debts.filter { debt ->
                        debt.dueDate < System.currentTimeMillis() && debt.status != "settled"
                    }
                    val upcomingDebts = debts.filter { debt ->
                        debt.dueDate >= System.currentTimeMillis() && debt.status != "settled"
                    }

                    val totalOwed = debts.filter { it.debtType == "owed" }.sumOf { it.amount }
                    val totalOwe = debts.filter { it.debtType == "owe" }.sumOf { it.amount }

                    _debtListState.update { state ->
                        state.copy(
                            debts = debts,
                            overdueDebts = overdueDebts,
                            upcomingDebts = upcomingDebts,
                            totalOwed = totalOwed,
                            totalOwe = totalOwe,
                            overdueCount = overdueDebts.size,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _debtListState.update {
                    it.copy(
                        error = e.message ?: "Error loading debts",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadDebtDetail(debtId: Long?) {
        viewModelScope.launch {
            _debtDetailState.update { it.copy(isLoading = true) }
            try {
                debtRepository.getDebtById(debtId).collect { debt ->
                    _debtDetailState.update {
                        it.copy(
                            debt = debt,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _debtDetailState.update {
                    it.copy(
                        error = e.message ?: "Error loading debt",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _debtListState.update { it.copy(selectedFilter = filter) }
    }

    fun initializeAddDebt() {
        _addEditState.update {
            AddEditDebtState(
                dueDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
            )
        }
    }

    fun initializeEditDebt(debtId: Long) {
        viewModelScope.launch {
            val debt = debtRepository.getDebtById(debtId).firstOrNull()

            if (debt == null) {
                _addEditState.update {
                    it.copy(
                        error = "Debt not found",
                        isEditMode = false
                    )
                }
                return@launch
            }

            _addEditState.update {
                AddEditDebtState(
                    personName = debt.personName,
                    amount = debt.amount.toString(),
                    debtType = debt.debtType,
                    dueDate = debt.dueDate,
                    status = debt.status,
                    description = debt.description,
                    reminderDays = debt.reminderDays.toString(),
                    reminderEnabled = debt.reminderEnabled,
                    notes = debt.notes,
                    isEditMode = true,
                    editingDebtId = debt.id
                )
            }
        }
    }


    fun updatePersonName(name: String) {
        _addEditState.update { it.copy(personName = name) }
    }

    fun updateAmount(amount: String) {
        _addEditState.update { it.copy(amount = amount) }
    }

    fun updateDebtType(type: String) {
        _addEditState.update { it.copy(debtType = type) }
    }

    fun updateDueDate(date: Long) {
        _addEditState.update { it.copy(dueDate = date) }
    }

    fun updateStatus(status: String) {
        _addEditState.update { it.copy(status = status) }
    }

    fun updateDescription(description: String) {
        _addEditState.update { it.copy(description = description) }
    }

    fun updateReminderDays(days: String) {
        _addEditState.update { it.copy(reminderDays = days) }
    }

    fun updateReminderEnabled(enabled: Boolean) {
        _addEditState.update { it.copy(reminderEnabled = enabled) }
    }

    fun updateNotes(notes: String) {
        _addEditState.update { it.copy(notes = notes) }
    }

    fun saveDebt() {
        viewModelScope.launch {
            val state = _addEditState.value

            // Validation
            if (state.personName.isBlank()) {
                _addEditState.update { it.copy(error = "Person name is required") }
                return@launch
            }

            if (state.amount.isBlank() || state.amount.toDoubleOrNull() == null) {
                _addEditState.update { it.copy(error = "Valid amount is required") }
                return@launch
            }

            _addEditState.update { it.copy(isLoading = true, error = null) }

            try {
                val debt = DebtEntity(
                    id = if (state.isEditMode) state.editingDebtId else null,
                    personName = state.personName,
                    amount = state.amount.toDouble(),
                    debtType = state.debtType,
                    dueDate = state.dueDate,
                    status = state.status,
                    description = state.description,
                    reminderDays = state.reminderDays.toIntOrNull() ?: 0,
                    reminderEnabled = state.reminderEnabled,
                    notes = state.notes,
                    lastModified = System.currentTimeMillis()
                )

                if (state.isEditMode) {
                    debtRepository.updateDebt(debt)
                } else {
                    debtRepository.insertDebt(debt)
                }

                _addEditState.update {
                    AddEditDebtState(
                        dueDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
                    )
                }

                loadDebts()
                _navigationEvent.emit(NavigationEvent.NavigateBack)

            } catch (e: Exception) {
                _addEditState.update {
                    it.copy(
                        error = e.message ?: "Error saving debt",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteDebt(debtId: Long) {
        viewModelScope.launch {
            try {
                debtRepository.deleteDebt(debtId)
                loadDebts()
            } catch (e: Exception) {
                _debtListState.update {
                    it.copy(error = e.message ?: "Error deleting debt")
                }
            }
        }
    }

    fun markDebtAsSettled(debtId: Long) {
        viewModelScope.launch {
            try {
                debtRepository.updateDebtStatus(debtId, "settled")
                loadDebts()
            } catch (e: Exception) {
                _debtListState.update {
                    it.copy(error = e.message ?: "Error updating debt")
                }
            }
        }
    }

    fun clearError() {
        _addEditState.update { it.copy(error = null) }
        _debtListState.update { it.copy(error = null) }
        _debtDetailState.update { it.copy(error = null) }
    }
}
