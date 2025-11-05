package ke.ac.ku.ledgerly.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import ke.ac.ku.ledgerly.auth.data.AuthRepository
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val transactionDao: TransactionDao
) {

    private fun getCurrentUserId(): String {
        return authRepository.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
    }

    suspend fun syncTransactions(deviceId: String): SyncResult {
        return try {
            if (!authRepository.isUserAuthenticated()) {
                return SyncResult.Error("User not authenticated")
            }

            val userId = getCurrentUserId()

            val localTransactions = transactionDao.getAllTransactionsSync()

            localTransactions.forEach { localTransaction ->
                val firestoreTransaction = FirestoreTransaction.fromEntity(localTransaction, userId, deviceId)
                firestore.collection("transactions")
                    .document(localTransaction.id.toString())
                    .set(firestoreTransaction, SetOptions.merge())
                    .await()
            }

            val remoteTransactions = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteTransactionList = remoteTransactions.documents.map { document ->
                FirestoreTransaction.toEntity(document.toObject<FirestoreTransaction>()!!)
            }

            remoteTransactionList.forEach { remoteTransaction ->
                transactionDao.insertTransaction(remoteTransaction)
            }

            SyncResult.Success(remoteTransactionList.size)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error during sync")
        }
    }

    suspend fun syncBudgets(deviceId: String): SyncResult {
        return try {
            val userId = getCurrentUserId()

            val localBudgets = transactionDao.getAllBudgetsSync()

            localBudgets.forEach { localBudget ->
                val firestoreBudget = FirestoreBudget.fromEntity(localBudget, userId, deviceId)
                // Use composite key of category and monthYear as document ID
                val documentId = "${localBudget.category}_${localBudget.monthYear}"
                firestore.collection("budgets")
                    .document(documentId)
                    .set(firestoreBudget, SetOptions.merge())
                    .await()
            }

            val remoteBudgets = firestore.collection("budgets")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteBudgetList = remoteBudgets.documents.map { document ->
                FirestoreBudget.toEntity(document.toObject<FirestoreBudget>()!!)
            }

            remoteBudgetList.forEach { remoteBudget ->
                transactionDao.insertBudget(remoteBudget)
            }

            SyncResult.Success(remoteBudgetList.size)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error during budget sync")
        }
    }

    suspend fun syncRecurringTransactions(deviceId: String): SyncResult {
        return try {
            val userId = getCurrentUserId()

            val localRecurringTransactions = transactionDao.getAllRecurringTransactionsSync()

            localRecurringTransactions.forEach { localRecurring ->
                val firestoreRecurring = FirestoreRecurringTransaction.fromEntity(localRecurring, userId, deviceId)
                firestore.collection("recurring_transactions")
                    .document(localRecurring.id.toString())
                    .set(firestoreRecurring, SetOptions.merge())
                    .await()
            }

            val remoteRecurringTransactions = firestore.collection("recurring_transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteRecurringList = remoteRecurringTransactions.documents.map { document ->
                FirestoreRecurringTransaction.toEntity(document.toObject<FirestoreRecurringTransaction>()!!)
            }

            remoteRecurringList.forEach { remoteRecurring ->
                transactionDao.insertRecurringTransaction(remoteRecurring)
            }

            SyncResult.Success(remoteRecurringList.size)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error during recurring transactions sync")
        }
    }

    suspend fun syncUserPreferences(darkMode: Boolean, syncEnabled: Boolean): SyncResult {
        return try {
            val userId = getCurrentUserId()
            val prefs = FirestoreUserPreferences.fromLocal(userId, darkMode, syncEnabled)

            firestore.collection("user_preferences")
                .document(userId)
                .set(prefs, SetOptions.merge())
                .await()

            val remoteDoc = firestore.collection("user_preferences")
                .document(userId)
                .get()
                .await()

            if (remoteDoc.exists()) {
                val remotePrefs = remoteDoc.toObject(FirestoreUserPreferences::class.java)
                remotePrefs?.let {
                    // TODO: Update local settings
                }
            }

            SyncResult.Success(1)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Failed to sync user preferences")
        }
    }

    /**
     * Performs a full sync of all data types.
     * Note: Partial failures are possible - check FullSyncResult.isSuccessful 
     * and individual result fields to determine which syncs completed.
     */
    suspend fun fullSync(deviceId: String): FullSyncResult {
        return try {
            val transactionResult = syncTransactions(deviceId)
            val budgetResult = syncBudgets(deviceId)
            val recurringResult = syncRecurringTransactions(deviceId)

//            val preferencesResult = syncUserPreferences(
//                darkMode =
//                syncEnabled =
//            )

            FullSyncResult(
                transactions = transactionResult,
                budgets = budgetResult,
                recurringTransactions = recurringResult
            )
        } catch (e: Exception) {
            FullSyncResult.Error(e.message ?: "Unknown error during full sync")
        }
    }
}

sealed class SyncResult {
    data class Success(val syncedCount: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

data class FullSyncResult(
    val transactions: SyncResult,
    val budgets: SyncResult,
    val recurringTransactions: SyncResult
) {
    companion object {
        fun Error(message: String): FullSyncResult {
            return FullSyncResult(
                transactions = SyncResult.Error(message),
                budgets = SyncResult.Error(message),
                recurringTransactions = SyncResult.Error(message)
            )
        }
    }

    val isSuccessful: Boolean
        get() = transactions is SyncResult.Success &&
                budgets is SyncResult.Success &&
                recurringTransactions is SyncResult.Success
}