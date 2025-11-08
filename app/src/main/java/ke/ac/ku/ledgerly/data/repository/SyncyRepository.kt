package ke.ac.ku.ledgerly.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import ke.ac.ku.ledgerly.data.dao.BudgetDao
import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.FirestoreBudget
import ke.ac.ku.ledgerly.data.model.FirestoreRecurringTransaction
import ke.ac.ku.ledgerly.data.model.FirestoreTransaction
import ke.ac.ku.ledgerly.domain.AuthStateProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authStateProvider: AuthStateProvider,
    private val transactionDao: TransactionDao,
    private val recurringTransactionDao: RecurringTransactionDao,
    private val budgetDao: BudgetDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    companion object {
        private const val TAG = "SyncRepository"
    }

    private suspend fun getCurrentUserId(): String {
        return authStateProvider.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
    }

    private suspend fun ensureAuthentication() {
        if (!authStateProvider.isUserAuthenticated()) {
            throw IllegalStateException("User not authenticated")
        }
    }

    private suspend fun isSyncEnabled(): Boolean {
        return userPreferencesRepository.syncEnabled.first()
    }

    suspend fun syncTransactions(deviceId: String): SyncResult {
        return try {
            ensureAuthentication()

            if (!isSyncEnabled()) {
                Log.d(TAG, "Sync is disabled, skipping transaction sync")
                return SyncResult.Success(0)
            }

            val userId = getCurrentUserId()
            Log.d(TAG, "Starting transactions sync for user: $userId")

            val localTransactions = transactionDao.getAllTransactionsSync()
            Log.d(TAG, "Found ${localTransactions.size} local transactions")

            val remoteSnapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteTransactionsMap = remoteSnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject<FirestoreTransaction>()?.let {
                        document.id to it
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing remote transaction ${document.id}: ${e.message}")
                    null
                }
            }.toMap()

            val batch = firestore.batch()
            var pushedCount = 0

            localTransactions.forEach { localTransaction ->
                try {
                    val docId = localTransaction.id.toString()
                    val remoteTransaction = remoteTransactionsMap[docId]

                    val shouldPush = remoteTransaction == null ||
                            (localTransaction.lastModified
                                ?: 0L) > remoteTransaction.lastModified.toDate().time

                    if (shouldPush) {
                        val firestoreTransaction =
                            FirestoreTransaction.fromEntity(localTransaction, userId, deviceId)
                        val docRef = firestore.collection("transactions").document(docId)
                        batch.set(docRef, firestoreTransaction, SetOptions.merge())
                        pushedCount++
                    } else {
                        Log.d(TAG, "Skipping push for transaction $docId - remote is newer")
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error preparing transaction ${localTransaction.id} for sync: ${e.message}"
                    )
                    throw e
                }
            }

            batch.commit().await()
            Log.d(TAG, "Successfully pushed $pushedCount transactions to Firestore")

            var pulledCount = 0
            remoteTransactionsMap.values.forEach { remoteTransaction ->
                try {
                    val localTransaction = localTransactions.find {
                        it.id.toString() == remoteTransaction.id
                    }

                    val shouldPull = localTransaction == null ||
                            remoteTransaction.lastModified.toDate().time > (localTransaction.lastModified
                        ?: 0L)

                    if (shouldPull) {
                        val entity = FirestoreTransaction.toEntity(remoteTransaction)
                        transactionDao.insertTransaction(entity)
                        pulledCount++
                    } else {
                        Log.d(
                            TAG,
                            "Skipping pull for transaction ${remoteTransaction.id} - local is newer"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating local transaction: ${e.message}")
                }
            }

            Log.d(TAG, "Transactions sync completed: pushed $pushedCount, pulled $pulledCount")
            SyncResult.Success(pulledCount)
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

            if (!isSyncEnabled()) {
                Log.d(TAG, "Sync is disabled, skipping recurring transaction sync")
                return SyncResult.Success(0)
            }

            val userId = getCurrentUserId()
            Log.d(TAG, "Starting recurring transactions sync for user: $userId")

            val localRecurringTransactions =
                recurringTransactionDao.getAllRecurringTransactionsSync()
            Log.d(TAG, "Found ${localRecurringTransactions.size} local recurring transactions")

            val remoteSnapshot = firestore.collection("recurring_transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteRecurringMap = remoteSnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject<FirestoreRecurringTransaction>()?.let {
                        document.id to it
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error parsing remote recurring transaction ${document.id}: ${e.message}"
                    )
                    null
                }
            }.toMap()

            val batch = firestore.batch()
            var pushedCount = 0

            localRecurringTransactions.forEach { localRecurring ->
                try {
                    val docId = localRecurring.id.toString()
                    val remoteRecurring = remoteRecurringMap[docId]

                    val shouldPush = remoteRecurring == null ||
                            (localRecurring.lastModified
                                ?: 0L) > remoteRecurring.lastModified.toDate().time

                    if (shouldPush) {
                        val firestoreRecurring = FirestoreRecurringTransaction.fromEntity(
                            localRecurring,
                            userId,
                            deviceId
                        )
                        val docRef = firestore.collection("recurring_transactions").document(docId)
                        batch.set(docRef, firestoreRecurring, SetOptions.merge())
                        pushedCount++
                    } else {
                        Log.d(
                            TAG,
                            "Skipping push for recurring transaction $docId - remote is newer"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error preparing recurring transaction ${localRecurring.id} for sync: ${e.message}"
                    )
                    throw e
                }
            }

            batch.commit().await()
            Log.d(TAG, "Successfully pushed $pushedCount recurring transactions to Firestore")

            var pulledCount = 0
            remoteRecurringMap.values.forEach { remoteRecurring ->
                try {
                    val localRecurring = localRecurringTransactions.find {
                        it.id.toString() == remoteRecurring.id
                    }

                    val shouldPull = localRecurring == null ||
                            remoteRecurring.lastModified.toDate().time > (localRecurring.lastModified
                        ?: 0L)

                    if (shouldPull) {
                        val entity = FirestoreRecurringTransaction.toEntity(remoteRecurring)
                        recurringTransactionDao.insertRecurringTransaction(entity)
                        pulledCount++
                    } else {
                        Log.d(
                            TAG,
                            "Skipping pull for recurring transaction ${remoteRecurring.id} - local is newer"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating local recurring transaction: ${e.message}")
                }
            }

            Log.d(
                TAG,
                "Recurring transactions sync completed: pushed $pushedCount, pulled $pulledCount"
            )
            SyncResult.Success(pulledCount)
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

    suspend fun syncBudgets(deviceId: String): SyncResult {
        return try {
            ensureAuthentication()

            if (!isSyncEnabled()) {
                Log.d(TAG, "Sync is disabled, skipping budget sync")
                return SyncResult.Success(0)
            }

            val userId = getCurrentUserId()
            Log.d(TAG, "Starting budgets sync for user: $userId")

            val localBudgets = budgetDao.getAllBudgetsSync()
            Log.d(TAG, "Found ${localBudgets.size} local budgets")

            val remoteSnapshot = firestore.collection("budgets")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteBudgetsMap = remoteSnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject<FirestoreBudget>()?.let {
                        document.id to it
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing remote budget ${document.id}: ${e.message}")
                    null
                }
            }.toMap()

            val batch = firestore.batch()
            var pushedCount = 0

            localBudgets.forEach { localBudget ->
                try {
                    val documentId = "${localBudget.category}_${localBudget.monthYear}"
                    val remoteBudget = remoteBudgetsMap[documentId]

                    val shouldPush = remoteBudget == null ||
                            (localBudget.lastModified
                                ?: 0L) > remoteBudget.lastModified.toDate().time

                    if (shouldPush) {
                        val firestoreBudget =
                            FirestoreBudget.fromEntity(localBudget, userId, deviceId)
                        val docRef = firestore.collection("budgets").document(documentId)
                        batch.set(docRef, firestoreBudget, SetOptions.merge())
                        pushedCount++
                    } else {
                        Log.d(TAG, "Skipping push for budget $documentId - remote is newer")
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error preparing budget ${localBudget.category} for sync: ${e.message}"
                    )
                    throw e
                }
            }

            batch.commit().await()
            Log.d(TAG, "Successfully pushed $pushedCount budgets to Firestore")


            var pulledCount = 0
            remoteBudgetsMap.values.forEach { remoteBudget ->
                try {
                    val localBudget = localBudgets.find {
                        it.category == remoteBudget.category && it.monthYear == remoteBudget.monthYear
                    }

                    val shouldPull = localBudget == null ||
                            remoteBudget.lastModified.toDate().time > (localBudget.lastModified
                        ?: 0L)

                    if (shouldPull) {
                        val entity = FirestoreBudget.toEntity(remoteBudget)
                        budgetDao.insertBudget(entity)
                        pulledCount++
                    } else {
                        Log.d(
                            TAG,
                            "Skipping pull for budget ${remoteBudget.category} - local is newer"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating local budget: ${e.message}")
                }
            }

            Log.d(TAG, "Budgets sync completed: pushed $pushedCount, pulled $pulledCount")
            SyncResult.Success(pulledCount)
        } catch (e: Exception) {
            Log.e(TAG, "Budgets sync failed", e)
            SyncResult.Error(e.message ?: "Unknown error during budget sync")
        }
    }

    suspend fun fullSync(deviceId: String): FullSyncResult {
        return try {
            Log.d(TAG, "Starting full sync...")

            if (!isSyncEnabled()) {
                Log.d(TAG, "Sync is disabled, skipping full sync")
                return FullSyncResult.Error("Sync is disabled")
            }

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
            if (!isSyncEnabled()) {
                Log.d(TAG, "Sync is disabled, skipping preferences sync")
                return SyncResult.Success(0)
            }

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