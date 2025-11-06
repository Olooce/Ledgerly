package ke.ac.ku.ledgerly.data.repository

import com.google.firebase.firestore.FirebaseFirestore
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

            // Push local to Firestore
            localTransactions.forEach { localTransaction ->
                val firestoreTransaction = FirestoreTransaction.fromEntity(localTransaction, userId, deviceId)
                firestore.collection("transactions")
                    .document(localTransaction.id.toString())
                    .set(firestoreTransaction, SetOptions.merge())
                    .await()
            }

            // Pull Firestore updates
            val remoteTransactions = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteTransactionList = remoteTransactions.documents.mapNotNull { document ->
                document.toObject<FirestoreTransaction>()?.let { FirestoreTransaction.toEntity(it) }
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

            // Push local
            localBudgets.forEach { localBudget ->
                val firestoreBudget = FirestoreBudget.fromEntity(localBudget, userId, deviceId)
                val documentId = "${localBudget.category}_${localBudget.monthYear}"
                firestore.collection("budgets")
                    .document(documentId)
                    .set(firestoreBudget, SetOptions.merge())
                    .await()
            }

            // Pull remote
            val remoteBudgets = firestore.collection("budgets")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteBudgetList = remoteBudgets.documents.mapNotNull { document ->
                document.toObject<FirestoreBudget>()?.let { FirestoreBudget.toEntity(it) }
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

            // Push local
            localRecurringTransactions.forEach { localRecurring ->
                val firestoreRecurring = FirestoreRecurringTransaction.fromEntity(localRecurring, userId, deviceId)
                firestore.collection("recurring_transactions")
                    .document(localRecurring.id.toString())
                    .set(firestoreRecurring, SetOptions.merge())
                    .await()
            }

            // Pull remote
            val remoteRecurringTransactions = firestore.collection("recurring_transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteRecurringList = remoteRecurringTransactions.documents.mapNotNull { document ->
                document.toObject<FirestoreRecurringTransaction>()?.let { FirestoreRecurringTransaction.toEntity(it) }
            }

            remoteRecurringList.forEach { remoteRecurring ->
                transactionDao.insertRecurringTransaction(remoteRecurring)
            }

            SyncResult.Success(remoteRecurringList.size)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error during recurring transactions sync")
        }
    }

    suspend fun syncUserPreferences(): SyncResult {
        return try {
            val userId = getCurrentUserId()

            // Push local preferences to Firestore
            val localPrefs = userPreferencesRepository.getCurrentPreferences()
            val firestorePrefs = FirestoreUserPreferences(
                userId = userId,
                userName = localPrefs.userName,
                currency = localPrefs.currency,
                monthlyBudget = localPrefs.monthlyBudget.toDoubleOrNull() ?: 0.0,
                notificationEnabled = localPrefs.notificationEnabled,
                onboardingCompleted = localPrefs.onboardingCompleted,
                lastUpdated = System.currentTimeMillis()
            )

            firestore.collection("user_preferences")
                .document(userId)
                .set(firestorePrefs, SetOptions.merge())
                .await()

            // Pull updated preferences
            userPreferencesRepository.loadFromFirestore()

            SyncResult.Success(1)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Failed to sync user preferences")
        }
    }

    suspend fun fullSync(deviceId: String): FullSyncResult {
        return try {
            val transactionResult = syncTransactions(deviceId)
            val budgetResult = syncBudgets(deviceId)
            val recurringResult = syncRecurringTransactions(deviceId)
            val preferencesResult = syncUserPreferences()

            FullSyncResult(
                transactions = transactionResult,
                budgets = budgetResult,
                recurringTransactions = recurringResult,
                preferences = preferencesResult
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
