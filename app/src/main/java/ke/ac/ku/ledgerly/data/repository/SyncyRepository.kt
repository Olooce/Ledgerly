package ke.ac.ku.ledgerly.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import ke.ac.ku.ledgerly.auth.data.AuthRepository
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.*
import ke.ac.ku.ledgerly.data.model.FirestoreUserPreferences
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val transactionDao: TransactionDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    companion object {
        private const val TAG = "SyncRepository"
    }

    private suspend fun getCurrentUserId(): String {
        return authRepository.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
    }

    private suspend fun ensureAuthentication() {
        if (!authRepository.isUserAuthenticated()) {
            throw IllegalStateException("User not authenticated")
        }
    }

    suspend fun syncTransactions(deviceId: String): SyncResult {
        return try {
            ensureAuthentication()
            val userId = getCurrentUserId()
            Log.d(TAG, "Starting transactions sync for user: $userId")

            val localTransactions = transactionDao.getAllTransactionsSync()
            Log.d(TAG, "Found ${localTransactions.size} local transactions")

            // Push local to Firestore with batch operation for better performance
            val batch = firestore.batch()
            localTransactions.forEach { localTransaction ->
                try {
                    val firestoreTransaction = FirestoreTransaction.fromEntity(localTransaction, userId, deviceId)
                    val docRef = firestore.collection("transactions")
                        .document(localTransaction.id.toString())
                    batch.set(docRef, firestoreTransaction, SetOptions.merge())
                } catch (e: Exception) {
                    Log.e(TAG, "Error preparing transaction ${localTransaction.id} for sync: ${e.message}")
                    throw e
                }
            }

            // Commit batch
            batch.commit().await()
            Log.d(TAG, "Successfully pushed ${localTransactions.size} transactions to Firestore")

            // Pull Firestore updates
            val remoteTransactions = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteTransactionList = remoteTransactions.documents.mapNotNull { document ->
                try {
                    document.toObject<FirestoreTransaction>()?.let { FirestoreTransaction.toEntity(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing remote transaction ${document.id}: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Retrieved ${remoteTransactionList.size} remote transactions")

            // Update local database
            remoteTransactionList.forEach { remoteTransaction ->
                transactionDao.insertTransaction(remoteTransaction)
            }

            Log.d(TAG, "Transactions sync completed successfully")
            SyncResult.Success(remoteTransactionList.size)
        } catch (e: Exception) {
            Log.e(TAG, "Transactions sync failed", e)
            when (e) {
                is FirebaseFirestoreException -> {
                    when (e.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                            SyncResult.Error("Permission denied. Check Firestore rules.")
                        FirebaseFirestoreException.Code.UNAVAILABLE ->
                            SyncResult.Error("Network unavailable. Please check your connection.")
                        else -> SyncResult.Error("Firestore error: ${e.message}")
                    }
                }
                is IllegalStateException ->
                    SyncResult.Error("Authentication error: ${e.message}")
                else ->
                    SyncResult.Error("Sync failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    suspend fun syncRecurringTransactions(deviceId: String): SyncResult {
        return try {
            ensureAuthentication()
            val userId = getCurrentUserId()
            Log.d(TAG, "Starting recurring transactions sync for user: $userId")

            val localRecurringTransactions = transactionDao.getAllRecurringTransactionsSync()
            Log.d(TAG, "Found ${localRecurringTransactions.size} local recurring transactions")

            // Push local to Firestore
            val batch = firestore.batch()
            localRecurringTransactions.forEach { localRecurring ->
                try {
                    val firestoreRecurring = FirestoreRecurringTransaction.fromEntity(localRecurring, userId, deviceId)
                    val docRef = firestore.collection("recurring_transactions")
                        .document(localRecurring.id.toString())
                    batch.set(docRef, firestoreRecurring, SetOptions.merge())
                } catch (e: Exception) {
                    Log.e(TAG, "Error preparing recurring transaction ${localRecurring.id} for sync: ${e.message}")
                    throw e
                }
            }

            batch.commit().await()
            Log.d(TAG, "Successfully pushed ${localRecurringTransactions.size} recurring transactions to Firestore")

            // Pull remote updates
            val remoteRecurringTransactions = firestore.collection("recurring_transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteRecurringList = remoteRecurringTransactions.documents.mapNotNull { document ->
                try {
                    document.toObject<FirestoreRecurringTransaction>()?.let { FirestoreRecurringTransaction.toEntity(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing remote recurring transaction ${document.id}: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Retrieved ${remoteRecurringList.size} remote recurring transactions")

            // Update local database
            remoteRecurringList.forEach { remoteRecurring ->
                transactionDao.insertRecurringTransaction(remoteRecurring)
            }

            Log.d(TAG, "Recurring transactions sync completed successfully")
            SyncResult.Success(remoteRecurringList.size)
        } catch (e: Exception) {
            Log.e(TAG, "Recurring transactions sync failed", e)
            when (e) {
                is FirebaseFirestoreException -> {
                    when (e.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                            SyncResult.Error("Permission denied. Check Firestore rules.")
                        FirebaseFirestoreException.Code.UNAVAILABLE ->
                            SyncResult.Error("Network unavailable. Please check your connection.")
                        else -> SyncResult.Error("Firestore error: ${e.message}")
                    }
                }
                is IllegalStateException ->
                    SyncResult.Error("Authentication error: ${e.message}")
                else ->
                    SyncResult.Error("Sync failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    // Keep your existing syncBudgets and syncUserPreferences methods as they're working
    suspend fun syncBudgets(deviceId: String): SyncResult {
        return try {
            ensureAuthentication()
            val userId = getCurrentUserId()
            Log.d(TAG, "Starting budgets sync for user: $userId")

            val localBudgets = transactionDao.getAllBudgetsSync()
            Log.d(TAG, "Found ${localBudgets.size} local budgets")

            // Push local
            val batch = firestore.batch()
            localBudgets.forEach { localBudget ->
                try {
                    val firestoreBudget = FirestoreBudget.fromEntity(localBudget, userId, deviceId)
                    val documentId = "${localBudget.category}_${localBudget.monthYear}"
                    val docRef = firestore.collection("budgets").document(documentId)
                    batch.set(docRef, firestoreBudget, SetOptions.merge())
                } catch (e: Exception) {
                    Log.e(TAG, "Error preparing budget ${localBudget.category} for sync: ${e.message}")
                    throw e
                }
            }

            batch.commit().await()
            Log.d(TAG, "Successfully pushed ${localBudgets.size} budgets to Firestore")

            // Pull remote
            val remoteBudgets = firestore.collection("budgets")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteBudgetList = remoteBudgets.documents.mapNotNull { document ->
                try {
                    document.toObject<FirestoreBudget>()?.let { FirestoreBudget.toEntity(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing remote budget ${document.id}: ${e.message}")
                    null
                }
            }

            Log.d(TAG, "Retrieved ${remoteBudgetList.size} remote budgets")

            remoteBudgetList.forEach { remoteBudget ->
                transactionDao.insertBudget(remoteBudget)
            }

            Log.d(TAG, "Budgets sync completed successfully")
            SyncResult.Success(remoteBudgetList.size)
        } catch (e: Exception) {
            Log.e(TAG, "Budgets sync failed", e)
            SyncResult.Error(e.message ?: "Unknown error during budget sync")
        }
    }

    suspend fun fullSync(deviceId: String): FullSyncResult {
        return try {
            Log.d(TAG, "Starting full sync...")

            val transactionResult = syncTransactions(deviceId)
            val budgetResult = syncBudgets(deviceId)
            val recurringResult = syncRecurringTransactions(deviceId)
            val preferencesResult = syncUserPreferences()

            val result = FullSyncResult(
                transactions = transactionResult,
                budgets = budgetResult,
                recurringTransactions = recurringResult,
                preferences = preferencesResult
            )

            if (result.isSuccessful) {
                Log.d(TAG, "Full sync completed successfully")
            } else {
                Log.e(TAG, "Full sync completed with errors: $result")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Full sync failed with exception", e)
            FullSyncResult.Error(e.message ?: "Unknown error during full sync")
        }
    }
    suspend fun syncUserPreferences(): SyncResult {
        return try {
            val pushResult = userPreferencesRepository.syncToFirestore()

            if (pushResult.isSuccess) {
                userPreferencesRepository.loadFromFirestore()
                SyncResult.Success(1)
            } else {
                SyncResult.Error("Failed to sync user preferences to cloud")
            }
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Failed to sync user preferences")
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
    val recurringTransactions: SyncResult,
    val preferences: SyncResult
) {
    companion object {
        fun Error(message: String): FullSyncResult {
            return FullSyncResult(
                transactions = SyncResult.Error(message),
                budgets = SyncResult.Error(message),
                recurringTransactions = SyncResult.Error(message),
                preferences = SyncResult.Error(message)
            )
        }
    }

    val isSuccessful: Boolean
        get() = listOf(
            transactions,
            budgets,
            recurringTransactions,
            preferences
        ).all { it is SyncResult.Success }
}
