package ke.ac.ku.ledgerly.presentation.bill_reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.BillReminderNavigationEvent
import ke.ac.ku.ledgerly.data.model.BillReminderEntity
import ke.ac.ku.ledgerly.data.model.BillReminderSummary
import ke.ac.ku.ledgerly.data.repository.BillReminderRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillReminderListState(
    val bills: List<BillReminderEntity> = emptyList(),
    val summary: BillReminderSummary = BillReminderSummary(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: String = "all" // all, upcoming, overdue, paid
)

data class AddEditBillReminderState(
    val billName: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val currency: String = "KES",
    val dueDate: Long = System.currentTimeMillis(),
    val category: String = "Utilities",
    val frequency: String = "once",
    val reminderDays: Int = 3,
    val reminderEnabled: Boolean = true,
    val paymentMethod: String = "",
    val notes: String = "",
    val color: Int = 0xFF6750A4.toInt(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val currentBillId: Long? = null
)

@HiltViewModel
class BillReminderViewModel @Inject constructor(
    private val billReminderRepository: BillReminderRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<BillReminderNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _listState = MutableStateFlow(BillReminderListState())
    val listState: StateFlow<BillReminderListState> = _listState

    private val _addEditState = MutableStateFlow(AddEditBillReminderState())
    val addEditState: StateFlow<AddEditBillReminderState> = _addEditState

    val allBills = billReminderRepository.getAllBillReminders()
        .map { bills ->
            bills.filter { it.status != "paid" && !it.isDeleted }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingBills = billReminderRepository.getAllBillReminders()
        .map { bills ->
            val now = System.currentTimeMillis()
            bills.filter { bill ->
                !bill.isDeleted &&
                        bill.status == "pending" &&
                        bill.dueDate >= now
            }.sortedBy { it.dueDate }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

     val overdueBills = billReminderRepository.getAllBillReminders()
        .map { bills ->
            val now = System.currentTimeMillis()
            bills.filter { bill ->
                !bill.isDeleted &&
                        bill.dueDate < now &&
                        (bill.status == "overdue" || bill.status == "pending")
            }.sortedBy { it.dueDate }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentBills = billReminderRepository.getRecentUpcomingBills(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSummary()
        updateBillStatuses()
    }

    private fun updateBillStatuses() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                billReminderRepository.getAllBillReminders().collect { bills ->
                    bills.forEach { bill ->
                        if (bill.status != "paid" && !bill.isDeleted) {
                            val isNowOverdue = bill.dueDate < now
                            val shouldUpdateStatus =
                                (isNowOverdue && bill.status != "overdue") ||
                                        (!isNowOverdue && bill.status == "overdue")

                            if (shouldUpdateStatus) {
                                val updatedBill = bill.copy(
                                    status = if (isNowOverdue) "overdue" else "pending",
                                    isOverdue = isNowOverdue,
                                    lastModified = now
                                )
                                billReminderRepository.updateBillReminder(updatedBill)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(error = e.message)
            }
        }
    }

    private fun loadSummary() {
        viewModelScope.launch {
            try {
                _listState.value = _listState.value.copy(isLoading = true)
                val summary = billReminderRepository.getBillReminderSummary()
                _listState.value = _listState.value.copy(
                    summary = summary,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setFilter(filter: String) {
        _listState.value = _listState.value.copy(selectedFilter = filter)
        loadSummary()
    }

    fun onAddNewBill() {
        viewModelScope.launch {
            _navigationEvent.emit(BillReminderNavigationEvent.NavigateToAddEditBill(null))
        }
    }

    fun onEditBill(billId: Long) {
        viewModelScope.launch {
            _navigationEvent.emit(BillReminderNavigationEvent.NavigateToAddEditBill(billId))
        }
    }

    fun onDeleteBill(billId: Long) {
        viewModelScope.launch {
            try {
                billReminderRepository.deleteBillReminder(billId)
                loadSummary()
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(error = e.message)
            }
        }
    }

    fun onMarkAsPaid(billId: Long) {
        viewModelScope.launch {
            try {
                billReminderRepository.updateBillStatus(billId, "paid", false)
                loadSummary()
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(error = e.message)
            }
        }
    }

    fun onBillReminderClick(billId: Long) {
        viewModelScope.launch {
            _navigationEvent.emit(BillReminderNavigationEvent.NavigateToBillDetail(billId))
        }
    }

    fun initializeAddEdit(billId: Long?) {
        viewModelScope.launch {
            try {
                if (billId != null) {
                    val bill = billReminderRepository.getBillReminderById(billId)
                    if (bill != null) {
                        _addEditState.value = AddEditBillReminderState(
                            billName = bill.billName,
                            description = bill.description,
                            amount = bill.amount,
                            currency = bill.currency,
                            dueDate = bill.dueDate,
                            category = bill.category,
                            frequency = bill.frequency,
                            reminderDays = bill.reminderDays,
                            reminderEnabled = bill.reminderEnabled,
                            paymentMethod = bill.paymentMethod,
                            notes = bill.notes,
                            color = bill.color,
                            isEditing = true,
                            currentBillId = billId
                        )
                    }
                } else {
                    _addEditState.value = AddEditBillReminderState()
                }
            } catch (e: Exception) {
                _addEditState.value = _addEditState.value.copy(error = e.message)
            }
        }
    }

    fun updateBillName(name: String) {
        _addEditState.value = _addEditState.value.copy(billName = name)
    }

    fun updateDescription(description: String) {
        _addEditState.value = _addEditState.value.copy(description = description)
    }

    fun updateAmount(amount: Double) {
        _addEditState.value = _addEditState.value.copy(amount = amount)
    }

    fun updateDueDate(date: Long) {
        _addEditState.value = _addEditState.value.copy(dueDate = date)
    }

    fun updateCategory(category: String) {
        _addEditState.value = _addEditState.value.copy(category = category)
    }

    fun updateFrequency(frequency: String) {
        _addEditState.value = _addEditState.value.copy(frequency = frequency)
    }

    fun updateReminderDays(days: Int) {
        _addEditState.value = _addEditState.value.copy(reminderDays = days)
    }

    fun updateReminderEnabled(enabled: Boolean) {
        _addEditState.value = _addEditState.value.copy(reminderEnabled = enabled)
    }

    fun updatePaymentMethod(method: String) {
        _addEditState.value = _addEditState.value.copy(paymentMethod = method)
    }

    fun updateNotes(notes: String) {
        _addEditState.value = _addEditState.value.copy(notes = notes)
    }

    fun updateColor(color: Int) {
        _addEditState.value = _addEditState.value.copy(color = color)
    }

    fun saveBillReminder() {
        viewModelScope.launch {
            try {
                val state = _addEditState.value

                // Validation
                if (state.billName.isBlank()) {
                    _addEditState.value = state.copy(error = "Bill name is required")
                    return@launch
                }

                if (state.amount <= 0) {
                    _addEditState.value = state.copy(error = "Amount must be greater than 0")
                    return@launch
                }

                _addEditState.value = state.copy(isLoading = true, error = null)

                val now = System.currentTimeMillis()
                val isOverdue = state.dueDate < now

                val billReminder = BillReminderEntity(
                    id = if (state.isEditing) state.currentBillId else null,
                    billName = state.billName,
                    description = state.description,
                    amount = state.amount,
                    currency = state.currency,
                    dueDate = state.dueDate,
                    category = state.category,
                    frequency = state.frequency,
                    nextDueDate = calculateNextDueDate(state.frequency, state.dueDate),
                    reminderDays = state.reminderDays,
                    reminderEnabled = state.reminderEnabled,
                    notificationSent = false,
                    isOverdue = isOverdue,
                    status = if (isOverdue) "overdue" else "pending",
                    paymentMethod = state.paymentMethod,
                    notes = state.notes,
                    color = state.color,
                    createdAt = if (state.isEditing) now else now,
                    lastModified = now,
                    isDeleted = false
                )

                if (state.isEditing && state.currentBillId != null) {
                    billReminderRepository.updateBillReminder(billReminder)
                } else {
                    billReminderRepository.insertBillReminder(billReminder)
                }

                _addEditState.value = state.copy(isLoading = false)
                loadSummary()
                _navigationEvent.emit(BillReminderNavigationEvent.NavigateBack)

            } catch (e: Exception) {
                _addEditState.value = _addEditState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun calculateNextDueDate(frequency: String, dueDate: Long): Long? {
        return when (frequency.lowercase()) {
            "monthly" -> dueDate + 30L * 24 * 60 * 60 * 1000
            "quarterly" -> dueDate + 90L * 24 * 60 * 60 * 1000
            "yearly" -> dueDate + 365L * 24 * 60 * 60 * 1000
            else -> null // "once"
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch {
            _navigationEvent.emit(BillReminderNavigationEvent.NavigateBack)
        }
    }
}