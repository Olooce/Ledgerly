package ke.ac.ku.ledgerly.domain

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import ke.ac.ku.ledgerly.data.dao.BudgetDao
import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CleanupManager @Inject constructor(
    private val transactionDao: TransactionDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val budgetDao: BudgetDao,
    private val firestore: FirebaseFirestore,
    private val authStateProvider: AuthStateProvider
) {
    companion object {
        private const val TAG = "CleanUpManager"
        private const val CLEANUP_THRESHOLD_DAYS = 30L
    }

    suspend fun cleanupOldDeletedItems() {
        try {
            val thresholdTime = System.currentTimeMillis() - (CLEANUP_THRESHOLD_DAYS * 24 * 60 * 60 * 1000)

            cleanupLocalDeletedItems(thresholdTime)

            if (authStateProvider.isUserAuthenticated()) {
                cleanupFirestoreDeletedItems(thresholdTime)
            }

            Log.d(TAG, "Cleanup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}", e)
        }
    }

    private suspend fun cleanupLocalDeletedItems(thresholdTime: Long) {
        val deletedTransactions = transactionDao.getAllTransactionsIncludingDeleted()
            .filter { it.isDeleted && (it.lastModified ?: 0L) < thresholdTime }

        Log.d(TAG, "Permanently deleting ${deletedTransactions.size} old transactions from local DB")

        val deletedRecurring = recurringTransactionDao.getAllRecurringTransactionsIncludingDeleted()
            .filter { it.isDeleted && (it.lastModified ?: 0L) < thresholdTime }

        Log.d(TAG, "Permanently deleting ${deletedRecurring.size} old recurring transactions from local DB")

        val deletedBudgets = budgetDao.getAllBudgetsIncludingDeleted()
            .filter { it.isDeleted && (it.lastModified ?: 0L) < thresholdTime }

        Log.d(TAG, "Permanently deleting ${deletedBudgets.size} old budgets from local DB")
    }

    private suspend fun cleanupFirestoreDeletedItems(thresholdTime: Long) {
        try {
            val userId = authStateProvider.getCurrentUserId() ?: return
            val thresholdTimestamp = Timestamp(thresholdTime / 1000, 0)

            val transactionsQuery = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDeleted", true)
                .whereLessThan("lastModified", thresholdTimestamp)
                .get()
                .await()

            val batch = firestore.batch()
            var count = 0

            transactionsQuery.documents.forEach { doc ->
                batch.delete(doc.reference)
                count++
            }

            val recurringQuery = firestore.collection("recurring_transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDeleted", true)
                .whereLessThan("lastModified", thresholdTimestamp)
                .get()
                .await()

            recurringQuery.documents.forEach { doc ->
                batch.delete(doc.reference)
                count++
            }

            val budgetsQuery = firestore.collection("budgets")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDeleted", true)
                .whereLessThan("lastModified", thresholdTimestamp)
                .get()
                .await()

            budgetsQuery.documents.forEach { doc ->
                batch.delete(doc.reference)
                count++
            }

            if (count > 0) {
                batch.commit().await()
                Log.d(TAG, "Permanently deleted $count old items from Firestore")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning Firestore: ${e.message}", e)
        }
    }
}