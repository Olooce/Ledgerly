package ke.ac.ku.ledgerly.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.HomeNavigationEvent
import ke.ac.ku.ledgerly.base.NavigationEvent
import ke.ac.ku.ledgerly.data.model.MonthlyTotals
import ke.ac.ku.ledgerly.data.model.PageRequest
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.data.repository.TransactionRepository
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import ke.ac.ku.ledgerly.utils.FormatingUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

data class HomePaginationState(
    val currentPage: Int = 0,
    val pageSize: Int = 10,
    val isLoading: Boolean = false,
    val hasNext: Boolean = true,
    val isRefreshing: Boolean = false
)

data class HomeState(
    val transactions: List<TransactionEntity> = emptyList(),
    val currentMonthTransactions: List<TransactionEntity> = emptyList(),
    val paginationState: HomePaginationState = HomePaginationState(),
    val currentMonth: String = "",
    val totalIncome: String = "0",
    val totalExpense: String = "0",
    val balance: String = "0",
    val isBalanceNegative: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    val userName: StateFlow<String> = userPreferencesRepository.userName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "User"
        )

    val currency: StateFlow<String> = userPreferencesRepository.currency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "KES"
        )

    init {
        loadInitialTransactions()
        loadSummary()
        updateCurrentMonth()
    }

    private fun updateCurrentMonth() {
        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        _homeState.update { it.copy(currentMonth = currentMonth) }
    }

    fun loadInitialTransactions() {
        _homeState.update {
            it.copy(
                transactions = emptyList(),
                currentMonthTransactions = emptyList(),
                paginationState = HomePaginationState(currentPage = 0, isRefreshing = true)
            )
        }
        loadTransactions(reset = true)
    }

    fun loadSummary() {
        viewModelScope.launch {
            val totals = runCatching { transactionRepository.getCurrentMonthTotals() }
                .getOrNull() ?: MonthlyTotals(0.0, 0.0)

            val income = totals.totalIncome ?: 0.0
            val expense = totals.totalExpense ?: 0.0
            val balanceValue = income - expense

            _homeState.update {
                it.copy(
                    totalIncome = FormatingUtils.formatCurrency(income),
                    totalExpense = FormatingUtils.formatCurrency(expense),
                    balance = FormatingUtils.formatCurrency(abs(balanceValue)),
                    isBalanceNegative = balanceValue < 0,
                    error = null
                )
            }
        }
    }




    fun loadTransactions(reset: Boolean = false) {
        val currentState = _homeState.value
        val paginationState = currentState.paginationState

        if (paginationState.isLoading || (!paginationState.hasNext && !reset)) {
            return
        }

        val pageToLoad = if (reset) 1 else paginationState.currentPage

        _homeState.update {
            it.copy(
                paginationState = it.paginationState.copy(
                    isLoading = true,
                    isRefreshing = reset && pageToLoad == 1
                ),
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val result = transactionRepository.getCurrentMonthTransactionsPaginated(
                    PageRequest(page = pageToLoad, pageSize = paginationState.pageSize)
                )

                _homeState.update { state ->
                    val newTransactions = if (reset) {
                        result.data
                    } else {
                        state.transactions + result.data
                    }

                    state.copy(
                        transactions = newTransactions,
                        paginationState = state.paginationState.copy(
                            currentPage = pageToLoad,
                            isLoading = false,
                            isRefreshing = false,
                            hasNext = result.hasNext
                        ),
                    )
                }
            } catch (e: Exception) {
                _homeState.update {
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

    fun onEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            when (event) {
                HomeUiEvent.OnSeeAllClicked -> {
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToSeeAll)
                }

                HomeUiEvent.OnAddIncomeClicked -> {
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToAddIncome)
                }

                HomeUiEvent.OnAddExpenseClicked -> {
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToAddExpense)
                }
            }
        }
    }

    fun clearTransactions() {
        _homeState.update {
            it.copy(
                transactions = emptyList(),
                currentMonthTransactions = emptyList(),
                paginationState = HomePaginationState()
            )
        }
    }
}

sealed class HomeUiEvent {
    object OnSeeAllClicked : HomeUiEvent()
    object OnAddIncomeClicked : HomeUiEvent()
    object OnAddExpenseClicked : HomeUiEvent()
}