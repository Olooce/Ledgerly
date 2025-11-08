package ke.ac.ku.ledgerly.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val dao: RecurringTransactionDao
) : ViewModel() {

    val recurringTransactions: Flow<List<RecurringTransactionEntity>> =
        dao.getAllRecurringTransactions()

    fun toggleRecurringTransactionStatus(id: Long, isActive: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateRecurringTransactionStatus(id, isActive)
        }
    }

    fun deleteRecurringTransaction(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val recurring = dao.getRecurringTransactionById(id)
            recurring?.let {
                dao.deleteRecurringTransaction(it)
            }
        }
    }

    private val _recurringTransactions = MutableStateFlow(emptyList<RecurringTransactionEntity>())


    private fun loadRecurringTransactions() {
        viewModelScope.launch {
            _recurringTransactions.value = dao.getAllRecurringTransactionsSync()
        }
    }

    init {
        loadRecurringTransactions()
    }
}
