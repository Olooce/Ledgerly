package ke.ac.ku.ledgerly.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ke.ac.ku.ledgerly.data.LedgerlyDatabase
import ke.ac.ku.ledgerly.data.dao.BillReminderDao
import ke.ac.ku.ledgerly.data.dao.BudgetDao
import ke.ac.ku.ledgerly.data.dao.CategoryDao
import ke.ac.ku.ledgerly.data.dao.DebtDao
import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
import ke.ac.ku.ledgerly.data.dao.SavingsGoalDao
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LedgerlyDatabase =
        LedgerlyDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideTransactionDao(db: LedgerlyDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideRecurringTransactionDao(db: LedgerlyDatabase): RecurringTransactionDao =
        db.recurringTransactionDao()

    @Provides
    @Singleton
    fun provideBudgetDao(db: LedgerlyDatabase): BudgetDao = db.budgetDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: LedgerlyDatabase): CategoryDao = db.categoryDao()

    @Provides
    @Singleton
    fun provideDebtDao(db: LedgerlyDatabase): DebtDao = db.debtDao()

    @Provides
    @Singleton
    fun provideSavingsGoalDao(db: LedgerlyDatabase): SavingsGoalDao = db.savingsGoalDao()

    @Provides
    @Singleton
    fun provideBillReminderDao(db: LedgerlyDatabase): BillReminderDao = db.billReminderDao()
}
