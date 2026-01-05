package ke.ac.ku.ledgerly.data.repository

import ke.ac.ku.ledgerly.data.dao.DebtDao
import ke.ac.ku.ledgerly.data.model.DebtEntity
import ke.ac.ku.ledgerly.data.model.DebtSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepository @Inject constructor(
    private val debtDao: DebtDao
) {
    fun getAllDebts(): Flow<List<DebtEntity>> = debtDao.getAllDebts()

    suspend fun getAllDebtSync(): List<DebtEntity> = debtDao.getAllDebtSync()

    fun getDebtById(id: Long): Flow<DebtEntity?> = debtDao.getDebtByIdFlow(id)

    fun getDebtsByType(debtType: String): Flow<List<DebtEntity>> = debtDao.getDebtsByType(debtType)

    fun getDebtsByStatus(status: String): Flow<List<DebtEntity>> = debtDao.getDebtsByStatus(status)

    fun getOverdueDebts(): Flow<List<DebtEntity>> = debtDao.getOverdueDebts()

    fun getUpcomingDebts(days: Int = 30): Flow<List<DebtEntity>> {
        val currentTime = System.currentTimeMillis()
        val endTime = currentTime + (days * 24 * 60 * 60 * 1000)
        return debtDao.getUpcomingDebts(currentTime, endTime)
    }

    suspend fun getDebtsNeedingReminder(): List<DebtEntity> = debtDao.getDebtsNeedingReminder()

    fun getDebtsSummary(): Flow<DebtSummary?> = debtDao.getDebtsSummary()

    suspend fun insertDebt(debt: DebtEntity): Long = debtDao.insertDebt(debt)

    suspend fun updateDebt(debt: DebtEntity) = debtDao.updateDebt(debt)

    suspend fun deleteDebt(id: Long) = debtDao.softDeleteDebt(id)

    suspend fun updateDebtStatus(id: Long, status: String) = debtDao.updateDebtStatus(id, status)

    suspend fun permanentlyDeleteOldDebts() = debtDao.permanentlyDeleteOldDebts()
}