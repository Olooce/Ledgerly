package ke.ac.ku.ledgerly.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.HomeNavigationEvent
import ke.ac.ku.ledgerly.base.NavigationEvent
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import ke.ac.ku.ledgerly.utils.FormatingUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: TransactionDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    val transactions: StateFlow<List<TransactionEntity>> =
        dao.getAllTransactions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

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

    fun getTotalExpense(transactions: List<TransactionEntity>): String {
        val total = transactions
            .filter { it.type == "Expense" }
            .sumOf { it.amount }
        return FormatingUtils.formatCurrency(total)
    }

    fun getTotalIncome(transactions: List<TransactionEntity>): String {
        val total = transactions
            .filter { it.type == "Income" }
            .sumOf { it.amount }
        return FormatingUtils.formatCurrency(total)
    }

    fun getBalance(transactions: List<TransactionEntity>): String {
        val income = transactions
            .filter { it.type == "Income" }
            .sumOf { it.amount }
        val expense = transactions
            .filter { it.type == "Expense" }
            .sumOf { it.amount }
        val balance = income - expense
        return FormatingUtils.formatCurrency(balance)
    }
}

sealed class HomeUiEvent {
    object OnSeeAllClicked : HomeUiEvent()
    object OnAddIncomeClicked : HomeUiEvent()
    object OnAddExpenseClicked : HomeUiEvent()
}