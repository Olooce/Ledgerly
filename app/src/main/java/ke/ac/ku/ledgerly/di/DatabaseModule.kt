package ke.ac.ku.ledgerly.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ke.ac.ku.ledgerly.data.LedgerlyDatabase
import ke.ac.ku.ledgerly.data.dao.BudgetDao
import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
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
    fun provideTransactionDao(db: LedgerlyDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideRecurringTransactionDao(db: LedgerlyDatabase): RecurringTransactionDao =
        db.recurringTransactionDao()

    @Provides
    fun provideBudgetDao(db: LedgerlyDatabase): BudgetDao = db.budgetDao()
}
