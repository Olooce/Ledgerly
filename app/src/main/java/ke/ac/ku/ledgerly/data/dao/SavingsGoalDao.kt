package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ke.ac.ku.ledgerly.data.model.SavingsGoalEntity
import ke.ac.ku.ledgerly.data.model.SavingsSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    
    @Query("SELECT * FROM savings_goals WHERE isDeleted = 0 ORDER BY targetDate ASC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>
    
    @Query("SELECT * FROM savings_goals WHERE isDeleted = 0 ORDER BY targetDate ASC")
    suspend fun getAllGoalsSync(): List<SavingsGoalEntity>
    
    @Query("SELECT * FROM savings_goals WHERE id = :id AND isDeleted = 0")
    suspend fun getGoalById(id: Long): SavingsGoalEntity?
    
    @Query("SELECT * FROM savings_goals WHERE id = :id AND isDeleted = 0")
    fun getGoalByIdFlow(id: Long): Flow<SavingsGoalEntity?>
    
    @Query("SELECT * FROM savings_goals WHERE isCompleted = 0 AND isDeleted = 0 ORDER BY targetDate ASC")
    fun getActiveGoals(): Flow<List<SavingsGoalEntity>>
    
    @Query("SELECT * FROM savings_goals WHERE isCompleted = 1 AND isDeleted = 0 ORDER BY lastModified DESC")
    fun getCompletedGoals(): Flow<List<SavingsGoalEntity>>

    @Query("""
    SELECT 
        SUM(targetAmount) AS totalTarget,
        SUM(currentAmount) AS totalSaved
    FROM savings_goals
    WHERE isDeleted = 0 AND isCompleted = 0
""")
    fun getSavingsSummary(): Flow<SavingsSummary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long
    
    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)
    
    @Query("UPDATE savings_goals SET isDeleted = 1, lastModified = :timestamp WHERE id = :id")
    suspend fun softDeleteGoal(id: Long, timestamp: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)
    
    @Query("UPDATE savings_goals SET currentAmount = :amount, lastModified = :timestamp WHERE id = :id")
    suspend fun updateGoalAmount(id: Long, amount: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE savings_goals SET isCompleted = 1, lastModified = :timestamp WHERE id = :id")
    suspend fun completeGoal(id: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM savings_goals WHERE isDeleted = 1")
    suspend fun permanentlyDeleteOldGoals()
}
