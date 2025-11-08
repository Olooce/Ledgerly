package ke.ac.ku.ledgerly.presentation.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecurringTransactionViewModel @Inject constructor(
    private val dao: TransactionDao
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
}