package ke.ac.ku.ledgerly.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val dao: RecurringTransactionDao,
    private val transactionDao: TransactionDao
) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

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
