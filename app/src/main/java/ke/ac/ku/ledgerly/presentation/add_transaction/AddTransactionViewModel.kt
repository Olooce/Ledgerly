package ke.ac.ku.ledgerly.presentation.add_transaction

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.AddTransactionNavigationEvent
import ke.ac.ku.ledgerly.base.BaseViewModel
import ke.ac.ku.ledgerly.base.NavigationEvent
import ke.ac.ku.ledgerly.base.UiEvent
import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.CategoryEntity
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.data.repository.BudgetRepository
import ke.ac.ku.ledgerly.data.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    val dao: TransactionDao,
    val recurringDao: RecurringTransactionDao,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {

    private val _transactionAdded = MutableSharedFlow<Unit>()
    val transactionAdded = _transactionAdded.asSharedFlow()

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategoriesFlow().collectLatest { categories ->
                _categories.value = categories
            }
        }
    }

    suspend fun addTransaction(transactionEntity: TransactionEntity): Boolean {
        return try {
            dao.insertTransactionWithTimestamp(transactionEntity)
            updateBudgetSpending(transactionEntity)
            true
        } catch (ex: Throwable) {
            Log.e("AddTransactionVM", "Failed to add transaction", ex)
            false
        }
    }

    suspend fun addRecurringTransaction(recurringTransaction: RecurringTransactionEntity): Boolean {
        return try {
            val recurringId: Long = recurringDao.insertRecurringTransaction(recurringTransaction)

            val firstTransaction = TransactionEntity(
                id = null,
                category = recurringTransaction.category,
                amount = recurringTransaction.amount,
                date = recurringTransaction.startDate,
                type = recurringTransaction.type,
                notes = recurringTransaction.notes + " (Recurring)",
                paymentMethod = recurringTransaction.paymentMethod,
                tags = recurringTransaction.tags
            )
            dao.insertTransaction(firstTransaction)

            recurringDao.updateRecurringTransaction(
                recurringTransaction.copy(
                    id = recurringId,
                    lastGeneratedDate = recurringTransaction.startDate
                )
            )

            updateBudgetSpending(firstTransaction)
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

            is AddTransactionUiEvent.OnAddRecurringTransactionClicked -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        val result = addRecurringTransaction(event.recurringTransaction)
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
                        "Alert: This expense will use ${
                            String.format(
                                java.util.Locale.US,
                                "%.1f",
                                newPercentage
                            )
                        }% of your ${transaction.category} budget"

                    else -> null
                }
            }
        }
        return null
    }

    private suspend fun updateBudgetSpending(transaction: TransactionEntity) {
        if (transaction.type == "Expense") {
            budgetRepository.refreshBudgetSpending()
            _transactionAdded.emit(Unit)
        }
    }
}

sealed class AddTransactionUiEvent : UiEvent() {
    data class OnAddTransactionClicked(val transactionEntity: TransactionEntity) :
        AddTransactionUiEvent()

    data class OnAddRecurringTransactionClicked(val recurringTransaction: RecurringTransactionEntity) :
        AddTransactionUiEvent()

    object OnBackPressed : AddTransactionUiEvent()
    object OnMenuClicked : AddTransactionUiEvent()
}