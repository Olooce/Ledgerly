package ke.ac.ku.ledgerly.feature.add_transaction

import androidx.lifecycle.viewModelScope
import ke.ac.ku.ledgerly.base.BaseViewModel
import ke.ac.ku.ledgerly.base.NavigationEvent
import ke.ac.ku.ledgerly.base.UiEvent
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.AddTransactionNavigationEvent
import ke.ac.ku.ledgerly.data.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(val dao: TransactionDao,  private val budgetRepository: BudgetRepository) : BaseViewModel() {


    suspend fun addTransaction(transactionEntity: TransactionEntity): Boolean {

        return try {
            dao.insertExpense(transactionEntity)
            updateBudgetSpending(transactionEntity)
            true
        } catch (ex: Throwable) {
            false
        }
    }

    override fun onEvent(event: UiEvent) {
        when (event) {
            is AddTransactionUiEvent.OnAddTransactionClicked -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        val result = addTransaction(event.transactionEntity)
                        if (result) {
                            _navigationEvent.emit(NavigationEvent.NavigateBack)
                        }
                    }
                }
            }

            is AddTransactionUiEvent.OnBackPressed -> {
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                }
            }

            is AddTransactionUiEvent.OnMenuClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(AddTransactionNavigationEvent.MenuOpenedClicked)
                }
            }
        }
    }

    private suspend fun checkBudgetAlert(transaction: TransactionEntity): String? {
        if (transaction.type == "Expense") {
            val budget = budgetRepository.getBudgetForCategory(transaction.category)
            budget?.let {
                val newSpending = it.currentSpending + transaction.amount
                val newPercentage = (newSpending / it.monthlyBudget) * 100

                return when {
                    newSpending > it.monthlyBudget ->
                        "Warning: This expense will exceed your ${transaction.category} budget!"
                    newPercentage >= 80 ->
                        "Alert: This expense will use ${String.format("%.1f", newPercentage)}% of your ${transaction.category} budget"
                    else -> null
                }
            }
        }
        return null
    }

    private suspend fun updateBudgetSpending(transaction: TransactionEntity) {
        if (transaction.type == "Expense") {
            budgetRepository.refreshBudgetSpending()
        }
    }
}

sealed class AddTransactionUiEvent : UiEvent() {
    data class OnAddTransactionClicked(val transactionEntity: TransactionEntity) : AddTransactionUiEvent()
    object OnBackPressed : AddTransactionUiEvent()
    object OnMenuClicked : AddTransactionUiEvent()
}


