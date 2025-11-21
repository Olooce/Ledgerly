package ke.ac.ku.ledgerly.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
import ke.ac.ku.ledgerly.data.model.PageRequest
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaginationState(
    val currentPage: Int = 1,
    val pageSize: Int = 20,
    val isLoading: Boolean = false,
    val hasNext: Boolean = true,
    val isRefreshing: Boolean = false
)

data class TransactionsState(
    val transactions: List<TransactionEntity> = emptyList(),
    val paginationState: PaginationState = PaginationState(),
    val filterType: String = "All",
    val searchQuery: String = "",
    val dateRange: String = "All Time",
    val amountRange: ClosedFloatingPointRange<Double>? = null,
    val selectedCategories: List<String> = emptyList(),
    val statusFilter: String = "All",
    val error: String? = null
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val dao: RecurringTransactionDao,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _transactionsState = MutableStateFlow(TransactionsState())
    val transactionsState = _transactionsState.asStateFlow()

    private val _recurringTransactions = MutableStateFlow(emptyList<RecurringTransactionEntity>())
    val recurringTransactionsState = _recurringTransactions.asStateFlow()

    init {
        loadInitialTransactions()
        loadRecurringTransactions()
    }

    fun loadInitialTransactions() {
        _transactionsState.update {
            it.copy(
                transactions = emptyList(),
                paginationState = PaginationState(currentPage = 0, isRefreshing = true)
            )
        }
        loadTransactions(reset = true)
    }

    fun loadTransactions(reset: Boolean = false) {
        val currentState = _transactionsState.value
        val paginationState = currentState.paginationState

        if (paginationState.isLoading || (!paginationState.hasNext && !reset)) {
            return
        }

        val pageToLoad = if (reset) 0 else paginationState.currentPage

        _transactionsState.update {
            it.copy(
                paginationState = it.paginationState.copy(
                    isLoading = true,
                    isRefreshing = reset && pageToLoad == 0
                ),
                error = null
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = transactionRepository.getFilteredTransactionsPaginated(
                    filterType = currentState.filterType,
                    searchQuery = currentState.searchQuery,
                    dateRange = currentState.dateRange,
                    amountRange = currentState.amountRange,
                    categories = currentState.selectedCategories,
                    PageRequest(page = pageToLoad + 1, pageSize = paginationState.pageSize)
                )

                _transactionsState.update { state ->
                    val newTransactions = if (reset) {
                        result.data
                    } else {
                        state.transactions + result.data
                    }

                    state.copy(
                        transactions = newTransactions,
                        paginationState = state.paginationState.copy(
                            currentPage = pageToLoad + 1,
                            isLoading = false,
                            isRefreshing = false,
                        )
                    )
                }
            } catch (e: Exception) {
                _transactionsState.update {
                    it.copy(
                        paginationState = it.paginationState.copy(
                            isLoading = false,
                            isRefreshing = false
                        ),
                        error = e.message
                    )
                }
            }
        }
    }

    fun updateAmountRange(amountRange: ClosedFloatingPointRange<Double>?) {
        _transactionsState.update { it.copy(amountRange = amountRange) }
        loadInitialTransactions()
    }

    fun updateDateRange(dateRange: String) {
        _transactionsState.update { it.copy(dateRange = dateRange) }
        loadInitialTransactions()
    }

    fun updateCategories(categories: List<String>) {
        _transactionsState.update { it.copy(selectedCategories = categories) }
        loadInitialTransactions()
    }

    fun updateStatusFilter(statusFilter: String) {
        _transactionsState.update { it.copy(statusFilter = statusFilter) }
        loadRecurringTransactions()
    }

    private fun loadRecurringTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentState = _transactionsState.value
                transactionRepository.getFilteredRecurringTransactionsFlow(
                    filterType = currentState.filterType,
                    searchQuery = currentState.searchQuery,
                    amountRange = currentState.amountRange,
                    categories = currentState.selectedCategories,
                    statusFilter = currentState.statusFilter
                ).collect { transactions ->
                    _recurringTransactions.value = transactions
                }
            } catch (e: Exception) {
                _transactionsState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    fun updateFilter(filterType: String) {
        _transactionsState.update { it.copy(filterType = filterType) }
        loadInitialTransactions()
    }

    fun updateSearchQuery(query: String) {
        _transactionsState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            if (_transactionsState.value.searchQuery == query) {
                loadInitialTransactions()
            }
        }
    }

    fun clearTransactions() {
        _transactionsState.update {
            it.copy(
                transactions = emptyList(),
                paginationState = PaginationState()
            )
        }
    }

    fun toggleRecurringTransactionStatus(id: Long, isActive: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateRecurringTransactionStatus(id, isActive)
            loadRecurringTransactions()
        }
    }

    fun deleteRecurringTransaction(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.softDeleteRecurringTransaction(id)
            loadRecurringTransactions()
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.deleteTransaction(transactionId)
            loadInitialTransactions()
        }
    }

}