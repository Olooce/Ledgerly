package ke.ac.ku.ledgerly.data.repository

import ke.ac.ku.ledgerly.data.dao.DebtDao
import ke.ac.ku.ledgerly.data.model.DebtEntity
import ke.ac.ku.ledgerly.data.model.DebtSummary
import ke.ac.ku.ledgerly.domain.CurrencyManager
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepository @Inject constructor(
    private val debtDao: DebtDao,
    private val currencyManager: CurrencyManager
) {
    fun getAllDebts(): Flow<List<DebtEntity>> = debtDao.getAllDebts()

    suspend fun getAllDebtSync(): List<DebtEntity> = debtDao.getAllDebtSync()

    fun getDebtById(id: Long?): Flow<DebtEntity?> = debtDao.getDebtByIdFlow(id)

    fun getDebtsByType(debtType: String): Flow<List<DebtEntity>> = debtDao.getDebtsByType(debtType)

    fun getDebtsByStatus(status: String): Flow<List<DebtEntity>> = debtDao.getDebtsByStatus(status)

    fun getOverdueDebts(): Flow<List<DebtEntity>> = debtDao.getOverdueDebts()

    fun getUpcomingDebts(days: Int = 30): Flow<List<DebtEntity>> {
        val currentTime = System.currentTimeMillis()
        val endTime = currentTime + (days * 24L * 60 * 60 * 1000)
        return debtDao.getUpcomingDebts(currentTime, endTime)
    }

    suspend fun getDebtsNeedingReminder(): List<DebtEntity> = debtDao.getDebtsNeedingReminder()

    fun getDebtsSummary(): Flow<DebtSummary?> = debtDao.getDebtsSummary()

    suspend fun insertDebt(debt: DebtEntity): Long = debtDao.insertDebt(debt)

    suspend fun updateDebt(debt: DebtEntity) = debtDao.updateDebt(debt)

    suspend fun deleteDebt(id: Long) = debtDao.softDeleteDebt(id)

    suspend fun markReminderSent(id: Long) = debtDao.markReminderSent(id)

    suspend fun updateDebtStatus(id: Long, status: String) = debtDao.updateDebtStatus(id, status)

    suspend fun permanentlyDeleteOldDebts() = debtDao.permanentlyDeleteOldDebts()

    suspend fun getDebtForDisplay(id: Long): DebtEntity? {
        val debt = debtDao.getDebtById(id) ?: return null
        return convertDebtForDisplay(debt)
    }

    private suspend fun convertDebtForDisplay(debt: DebtEntity): DebtEntity {
        val displayCurrency = currencyManager.getDisplayCurrency()
        if (displayCurrency == "USD") {
            return debt
        }

        return debt
    }

    suspend fun getTotalOwedUsd(): BigDecimal {
        val debts = getAllDebtSync().filter { it.debtType == "owed" && !it.isDeleted }
        return debts.fold(BigDecimal.ZERO) { acc, debt ->
            acc + debt.amount
        }
    }

    suspend fun getTotalOweUsd(): BigDecimal {
        val debts = getAllDebtSync().filter { it.debtType == "owe" && !it.isDeleted }
        return debts.fold(BigDecimal.ZERO) { acc, debt ->
            acc + debt.amount
        }
    }

    suspend fun getTotalPendingUsd(): BigDecimal {
        val debts = getAllDebtSync().filter { it.status == "pending" && !it.isDeleted }
        return debts.fold(BigDecimal.ZERO) { acc, debt ->
            acc + debt.amount
        }
    }
}