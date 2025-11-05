package ke.ac.ku.ledgerly.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ke.ac.ku.ledgerly.data.LedgerlyDatabase
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LedgerlyDatabase {
        return LedgerlyDatabase.getInstance(context)
    }

    @Provides
    fun provideExpenseDao(database: LedgerlyDatabase): TransactionDao {
        return database.expenseDao()
    }
}
