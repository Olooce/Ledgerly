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


    @Transaction
    suspend fun updateGoalAmountWithMilestones(
        goalId: Long,
        newAmount: Double
    ): List<Double> {
        val goal = getGoalByIdOnce(goalId) ?: return emptyList()

        val oldPercentage = (goal.currentAmount / goal.targetAmount) * 100
        val newPercentage = (newAmount / goal.targetAmount) * 100

        val milestones = listOf(25.0, 50.0, 75.0, 100.0)

        val crossed = milestones.filter {
            it > goal.lastMilestoneReached &&
                    oldPercentage < it &&
                    newPercentage >= it
        }

        updateGoal(
            goal.copy(
                currentAmount = newAmount,
                lastMilestoneReached = crossed.maxOrNull() ?: goal.lastMilestoneReached
            )
        )

        return crossed
    }

}
