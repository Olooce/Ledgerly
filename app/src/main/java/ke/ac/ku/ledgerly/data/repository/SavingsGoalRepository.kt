package ke.ac.ku.ledgerly.data.repository

import androidx.room.Transaction
import ke.ac.ku.ledgerly.data.dao.SavingsGoalDao
import ke.ac.ku.ledgerly.data.model.SavingsGoalEntity
import ke.ac.ku.ledgerly.data.model.SavingsSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SavingsGoalRepository @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao
) {
    fun getAllGoals(): Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()

    fun getActiveGoals(): Flow<List<SavingsGoalEntity>> = savingsGoalDao.getActiveGoals()

    fun getCompletedGoals(): Flow<List<SavingsGoalEntity>> = savingsGoalDao.getCompletedGoals()

    fun getGoalById(id: Long): Flow<SavingsGoalEntity?> = savingsGoalDao.getGoalByIdFlow(id)

    suspend fun getGoalByIdOnce(id: Long): SavingsGoalEntity? =
        savingsGoalDao.getGoalById(id)


    fun getSavingsSummary(): Flow<SavingsSummary> =
        savingsGoalDao.getSavingsSummary()

    suspend fun insertGoal(goal: SavingsGoalEntity): Long = savingsGoalDao.insertGoal(goal)

    suspend fun updateGoal(goal: SavingsGoalEntity) = savingsGoalDao.updateGoal(goal)

    suspend fun updateGoalAmount(id: Long, amount: Double) =
        savingsGoalDao.updateGoalAmount(id, amount)

    suspend fun completeGoal(id: Long) = savingsGoalDao.completeGoal(id)

    suspend fun deleteGoal(id: Long) = savingsGoalDao.softDeleteGoal(id)

    suspend fun permanentlyDeleteOldGoals() = savingsGoalDao.permanentlyDeleteOldGoals()

    suspend fun getAllGoalsSync(): List<SavingsGoalEntity> = savingsGoalDao.getAllGoalsSync()


    suspend fun updateGoalAmountWithMilestones(
        goalId: Long,
        newAmount: Double
    ): List<Double> {
        return savingsGoalDao.updateGoalAmountWithMilestones(goalId, newAmount)
    }

}
