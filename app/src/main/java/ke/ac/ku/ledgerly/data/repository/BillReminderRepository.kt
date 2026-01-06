package ke.ac.ku.ledgerly.data.repository

import ke.ac.ku.ledgerly.data.dao.BillReminderDao
import ke.ac.ku.ledgerly.data.model.BillReminderEntity
import ke.ac.ku.ledgerly.data.model.BillReminderSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BillReminderRepository @Inject constructor(
    private val billReminderDao: BillReminderDao
) {

    // Get all bill reminders
    fun getAllBillReminders(): Flow<List<BillReminderEntity>> =
        billReminderDao.getAllBillReminders()

    // Get upcoming bills within a time range
    fun getUpcomingBills(
        startDate: Long = System.currentTimeMillis(),
        endDate: Long = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000) // 30 days
    ): Flow<List<BillReminderEntity>> =
        billReminderDao.getUpcomingBills(startDate, endDate)

    // Get overdue bills
    fun getOverdueBills(currentTime: Long = System.currentTimeMillis()): Flow<List<BillReminderEntity>> =
        billReminderDao.getOverdueBills(currentTime)

    // Get bills that need reminders
    suspend fun getBillsNeedingReminder(): List<BillReminderEntity> {
        val reminderTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // Next 24 hours
        return billReminderDao.getBillsNeedingReminder(reminderTime)
    }

    // Get bills by category
    fun getBillsByCategory(category: String): Flow<List<BillReminderEntity>> =
        billReminderDao.getBillsByCategory(category)

    // Get recent upcoming bills (for dashboard)
    fun getRecentUpcomingBills(limit: Int = 5): Flow<List<BillReminderEntity>> =
        billReminderDao.getUpcomingBillsLimited(limit)

    // Get bill by ID
    suspend fun getBillReminderById(id: Long): BillReminderEntity? =
        billReminderDao.getBillReminderById(id)

    // Get bill reminder summary
    suspend fun getBillReminderSummary(): BillReminderSummary {
        val allBills = billReminderDao.getAllBillRemindersOnce()
        val currentTime = System.currentTimeMillis()
        val thirtyDaysFromNow = currentTime + (30 * 24 * 60 * 60 * 1000)

        val upcomingBills = allBills.filter {
            it.status == "pending" && it.dueDate in currentTime..thirtyDaysFromNow
        }

        val overdueBills = allBills.filter {
            it.status == "pending" && it.dueDate < currentTime
        }

        val totalAmount = upcomingBills.sumOf { it.amount }

        return BillReminderSummary(
            totalUpcoming = upcomingBills.size,
            totalAmount = totalAmount,
            overdueCount = overdueBills.size,
            thisMonthCount = upcomingBills.size
        )
    }

    // Insert bill reminder
    suspend fun insertBillReminder(billReminder: BillReminderEntity): Long =
        billReminderDao.insertBillReminder(billReminder)

    // Update bill reminder
    suspend fun updateBillReminder(billReminder: BillReminderEntity) =
        billReminderDao.updateBillReminder(billReminder)

    // Delete bill reminder (soft delete)
    suspend fun deleteBillReminder(id: Long) =
        billReminderDao.softDeleteBillReminder(id)

    // Mark reminder as sent
    suspend fun markReminderSent(id: Long) =
        billReminderDao.markReminderSent(id)

    // Update bill status
    suspend fun updateBillStatus(id: Long, status: String, isOverdue: Boolean = false) =
        billReminderDao.updateBillStatus(id, status, isOverdue)

    // Get total upcoming amount
    suspend fun getTotalUpcomingAmount(
        startDate: Long = System.currentTimeMillis(),
        endDate: Long = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000)
    ): Double {
        return billReminderDao.getTotalUpcomingAmount(startDate, endDate) ?: 0.0
    }

    // Get overdue bills count
    suspend fun getOverdueBillsCount(): Int =
        billReminderDao.getOverdueBillsCount(System.currentTimeMillis())
}
