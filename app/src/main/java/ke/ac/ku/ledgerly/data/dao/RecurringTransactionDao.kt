package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransactionEntity): Long

    suspend fun insertRecurringTransactionWithTimestamp(recurringTransaction: RecurringTransactionEntity) {
        insertRecurringTransaction(recurringTransaction.copy(lastModified = System.currentTimeMillis()))
    }

    @Update
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransactionEntity)

    @Query("UPDATE recurring_transactions SET isDeleted = 1, lastModified = :timestamp WHERE id = :id")
    suspend fun softDeleteRecurringTransaction(
        id: Long?,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND isDeleted = 0 ORDER BY startDate DESC")
    suspend fun getActiveRecurringTransactions(): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions WHERE isDeleted = 0 ORDER BY startDate DESC")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id AND isDeleted = 0")
    suspend fun getRecurringTransactionById(id: Long): RecurringTransactionEntity?

    @Query("UPDATE recurring_transactions SET isActive = :isActive WHERE id = :id")
    suspend fun updateRecurringTransactionStatus(id: Long, isActive: Boolean)

    @Query("SELECT * FROM recurring_transactions WHERE isDeleted = 0 ORDER BY startDate DESC")
    suspend fun getAllRecurringTransactionsSync(): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions ORDER BY startDate DESC")
    suspend fun getAllRecurringTransactionsIncludingDeleted(): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions WHERE isDeleted = 0 ORDER BY startDate DESC LIMIT :limit OFFSET :offset")
    suspend fun getRecurringTransactionsPaginated(
        limit: Int,
        offset: Int
    ): List<RecurringTransactionEntity>

    @Query("SELECT COUNT(*) FROM recurring_transactions WHERE isDeleted = 0")
    suspend fun getRecurringTransactionsCount(): Int

    @Query("SELECT * FROM recurring_transactions WHERE isDeleted = 0 ORDER BY startDate DESC LIMIT :limit OFFSET :offset")
    fun getRecurringTransactionsPaginatedFlow(
        limit: Int,
        offset: Int
    ): Flow<List<RecurringTransactionEntity>>

    @Query(
        """
    SELECT * FROM recurring_transactions 
    WHERE isDeleted = 0 
    AND (:filterType = 'All' OR type = :filterType)
    AND (:searchQuery = '' OR category LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%')
    AND (:minAmount = -1 OR amount >= :minAmount)
    AND (:maxAmount = -1 OR amount <= :maxAmount)
    AND (:categoriesCount = 0 OR category IN (:categories))
    AND (:statusFilter = 'All' OR 
         (:statusFilter = 'Active' AND isActive = 1) OR 
         (:statusFilter = 'Paused' AND isActive = 0))
    ORDER BY startDate DESC
    LIMIT :limit OFFSET :offset
"""
    )
    suspend fun getFilteredRecurringTransactionsPaginated(
        filterType: String,
        searchQuery: String,
        minAmount: Double,
        maxAmount: Double,
        categoriesCount: Int,
        categories: List<String>,
        statusFilter: String,
        limit: Int,
        offset: Int
    ): List<RecurringTransactionEntity>

    @Query(
        """
    SELECT COUNT(*) FROM recurring_transactions 
    WHERE isDeleted = 0 
    AND (:filterType = 'All' OR type = :filterType)
    AND (:searchQuery = '' OR category LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%')
    AND (:minAmount = -1 OR amount >= :minAmount)
    AND (:maxAmount = -1 OR amount <= :maxAmount)
    AND (:categoriesCount = 0 OR category IN (:categories))
    AND (:statusFilter = 'All' OR 
         (:statusFilter = 'Active' AND isActive = 1) OR 
         (:statusFilter = 'Paused' AND isActive = 0))
"""
    )
    suspend fun getFilteredRecurringTransactionsCount(
        filterType: String,
        searchQuery: String,
        minAmount: Double,
        maxAmount: Double,
        categoriesCount: Int,
        categories: List<String>,
        statusFilter: String
    ): Int

    @Query(
        """
    SELECT * FROM recurring_transactions 
    WHERE isDeleted = 0 
    AND (:filterType = 'All' OR type = :filterType)
    AND (:searchQuery = '' OR category LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%')
    AND (:minAmount = -1 OR amount >= :minAmount)
    AND (:maxAmount = -1 OR amount <= :maxAmount)
    AND (:categoriesCount = 0 OR category IN (:categories))
    AND (:statusFilter = 'All' OR 
         (:statusFilter = 'Active' AND isActive = 1) OR 
         (:statusFilter = 'Paused' AND isActive = 0))
    ORDER BY startDate DESC
"""
    )
    fun getFilteredRecurringTransactionsFlow(
        filterType: String,
        searchQuery: String,
        minAmount: Double,
        maxAmount: Double,
        categoriesCount: Int,
        categories: List<String>,
        statusFilter: String
    ): Flow<List<RecurringTransactionEntity>>

}