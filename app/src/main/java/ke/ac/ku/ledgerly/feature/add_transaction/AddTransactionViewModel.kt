package ke.ac.ku.ledgerly.feature.add_transaction

import androidx.lifecycle.viewModelScope
import ke.ac.ku.ledgerly.base.BaseViewModel
import ke.ac.ku.ledgerly.base.NavigationEvent
import ke.ac.ku.ledgerly.base.UiEvent
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.AddTransactionNavigationEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(val dao: TransactionDao) : BaseViewModel() {


    suspend fun addTransaction(transactionEntity: TransactionEntity): Boolean {
        return try {
            dao.insertExpense(transactionEntity)
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
}

sealed class AddTransactionUiEvent : UiEvent() {
    data class OnAddTransactionClicked(val transactionEntity: TransactionEntity) : AddTransactionUiEvent()
    object OnBackPressed : AddTransactionUiEvent()
    object OnMenuClicked : AddTransactionUiEvent()
}


