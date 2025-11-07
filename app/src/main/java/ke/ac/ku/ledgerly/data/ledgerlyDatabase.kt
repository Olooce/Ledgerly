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
    version = 6,
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
//                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)

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
                monthYear TEXT NOT NULL,
                lastModified INTEGER
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No schema change, empty migration
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Transactions table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                date INTEGER NOT NULL,
                type TEXT NOT NULL,
                notes TEXT NOT NULL,
                paymentMethod TEXT NOT NULL,
                tags TEXT NOT NULL,
                lastModified INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO transactions_new (id, category, amount, date, type, notes, paymentMethod, tags)
            SELECT 
                id,
                category,
                amount,
                CASE
                    WHEN date IS NULL OR TRIM(date) = '' THEN CAST(strftime('%s', 'now') * 1000 AS INTEGER)
                    ELSE CAST(strftime('%s', date) * 1000 AS INTEGER)
                END AS date,
                type,
                notes,
                paymentMethod,
                tags
            FROM transactions
            """.trimIndent()
        )

        db.execSQL("DROP TABLE transactions")
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        // Recurring transactions table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS recurring_transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                type TEXT NOT NULL,
                notes TEXT NOT NULL,
                paymentMethod TEXT NOT NULL,
                tags TEXT NOT NULL,
                frequency TEXT NOT NULL,
                startDate INTEGER NOT NULL,
                endDate INTEGER,
                lastGeneratedDate INTEGER,
                isActive INTEGER NOT NULL DEFAULT 1,
                lastModified INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO recurring_transactions_new (
                id, category, amount, type, notes, paymentMethod, tags, frequency, startDate, endDate, lastGeneratedDate, isActive
            )
            SELECT
                id,
                category,
                amount,
                type,
                notes,
                paymentMethod,
                tags,
                frequency,
                CASE
                    WHEN startDate IS NULL OR TRIM(startDate) = '' THEN CAST(strftime('%s', 'now') * 1000 AS INTEGER)
                    ELSE CAST(strftime('%s', startDate) * 1000 AS INTEGER)
                END AS startDate,
                CASE
                    WHEN endDate IS NULL OR TRIM(endDate) = '' THEN NULL
                    ELSE CAST(strftime('%s', endDate) * 1000 AS INTEGER)
                END AS endDate,
                CASE
                    WHEN lastGeneratedDate IS NULL OR TRIM(lastGeneratedDate) = '' THEN NULL
                    ELSE CAST(strftime('%s', lastGeneratedDate) * 1000 AS INTEGER)
                END AS lastGeneratedDate,
                isActive
            FROM recurring_transactions
            """.trimIndent()
        )

        db.execSQL("DROP TABLE recurring_transactions")
        db.execSQL("ALTER TABLE recurring_transactions_new RENAME TO recurring_transactions")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {

    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE transactions ADD COLUMN lastModified INTEGER")
        database.execSQL("ALTER TABLE budgets ADD COLUMN lastModified INTEGER")
        database.execSQL("UPDATE budgets SET lastModified = strftime('%s','now') * 1000")
        database.execSQL("ALTER TABLE recurring_transactions ADD COLUMN lastModified INTEGER")
    }
}





