package ke.ac.ku.ledgerly.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.data.model.Converters
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import javax.inject.Singleton

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        RecurringTransactionEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
@Singleton
abstract class LedgerlyDatabase : RoomDatabase() {

    abstract fun expenseDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "ledgerly_db"

        @Volatile
        private var INSTANCE: LedgerlyDatabase? = null

        fun getInstance(@ApplicationContext context: Context): LedgerlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LedgerlyDatabase::class.java,
                    DATABASE_NAME
                )
                    //                    .addMigrations(
                    //                        MIGRATION_1_2,
                    //                        MIGRATION_2_3,
                    //                        MIGRATION_3_4
                    //                    )
                    .fallbackToDestructiveMigration(true) //  Delete and recreate the database: For Dev
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS budgets (
                category TEXT PRIMARY KEY NOT NULL,
                monthlyBudget REAL NOT NULL,
                currentSpending REAL NOT NULL DEFAULT 0.0,
                monthYear TEXT NOT NULL
            )
        """.trimIndent()
        )
    }

}

val MIGRATION_2_3 = object : Migration(2, 3) {

}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS recurring_transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                type TEXT NOT NULL,
                notes TEXT NOT NULL,
                paymentMethod TEXT NOT NULL,
                tags TEXT NOT NULL,
                frequency TEXT NOT NULL,
                startDate TEXT NOT NULL,
                endDate TEXT,
                lastGeneratedDate TEXT,
                isActive INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()
        )
    }
}