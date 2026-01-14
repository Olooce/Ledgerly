package ke.ac.ku.ledgerly.data.repository

import ke.ac.ku.ledgerly.data.dao.SavingsGoalDao
import ke.ac.ku.ledgerly.data.model.SavingsGoalEntity
import ke.ac.ku.ledgerly.data.model.SavingsSummary
import ke.ac.ku.ledgerly.domain.CurrencyManager
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject

class SavingsGoalRepository @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao,
    private val currencyManager: CurrencyManager
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

    suspend fun updateGoalAmount(id: Long, amountUsd: BigDecimal) =
        savingsGoalDao.updateGoalAmount(id, amountUsd)

    suspend fun completeGoal(id: Long) = savingsGoalDao.completeGoal(id)

    suspend fun deleteGoal(id: Long) = savingsGoalDao.softDeleteGoal(id)

    suspend fun permanentlyDeleteOldGoals() = savingsGoalDao.permanentlyDeleteOldGoals()

    suspend fun getAllGoalsSync(): List<SavingsGoalEntity> = savingsGoalDao.getAllGoalsSync()


    suspend fun updateGoalAmountWithMilestones(
        goalId: Long,
        newAmountUsd: BigDecimal
    ): List<BigDecimal> {
        return savingsGoalDao.updateGoalAmountWithMilestones(goalId, newAmountUsd)
    }

    suspend fun getGoalForDisplay(id: Long): SavingsGoalEntity? {
        val goal = getGoalByIdOnce(id) ?: return null
        return convertGoalForDisplay(goal)
    }

    private suspend fun convertGoalForDisplay(goal: SavingsGoalEntity): SavingsGoalEntity {
        val displayCurrency = currencyManager.getDisplayCurrency()
        if (displayCurrency == "USD") {
            return goal
        }

        // Convert amounts for display only
        val displayTargetUsd =
            currencyManager.convertToDisplayCurrency(goal.targetAmount, displayCurrency)
        val displayCurrentUsd =
            currencyManager.convertToDisplayCurrency(goal.currentAmount, displayCurrency)
        val displayMilestoneUsd = currencyManager.convertToDisplayCurrency(
            goal.lastMilestoneReached,
            targetCurrency = displayCurrency
        )

        return goal.copy(
            targetAmount = displayTargetUsd.toBigDecimal(),
            currentAmount = displayCurrentUsd.toBigDecimal(),
            lastMilestoneReached = displayMilestoneUsd.toBigDecimal()
        )
    }

    suspend fun getTotalTargetUsd(): BigDecimal {
        val goals = getAllGoalsSync().filter { !it.isDeleted && !it.isCompleted }
        return goals.fold(BigDecimal.ZERO) { acc, goal ->
            acc + goal.targetAmount
        }
    }

    suspend fun getTotalSavedUsd(): BigDecimal {
        val goals = getAllGoalsSync().filter { !it.isDeleted && !it.isCompleted }
        return goals.fold(BigDecimal.ZERO) { acc, goal ->
            acc + goal.currentAmount
        }
    }

}
