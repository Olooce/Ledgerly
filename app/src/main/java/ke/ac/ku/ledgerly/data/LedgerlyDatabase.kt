package ke.ac.ku.ledgerly.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.ac.ku.ledgerly.data.dao.BudgetDao
import ke.ac.ku.ledgerly.data.dao.CategoryDao
import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.data.model.CategoryEntity
import ke.ac.ku.ledgerly.data.model.Converters
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import javax.inject.Singleton

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        RecurringTransactionEntity::class,
        CategoryEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
@Singleton
abstract class LedgerlyDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao

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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8
                    )
//                    .fallbackToDestructiveMigration(true) //  Delete and recreate the database: For Dev
                    .build()

                INSTANCE = instance
                instance
            }
        }


    }

    fun clearAllData() {
        clearAllTables()
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
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN lastModified INTEGER")
        db.execSQL("ALTER TABLE budgets ADD COLUMN lastModified INTEGER")
        db.execSQL("UPDATE budgets SET lastModified = strftime('%s','now') * 1000")
        db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN lastModified INTEGER")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE budgets ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create categories table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categories (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                icon INTEGER NOT NULL,
                color INTEGER NOT NULL,
                isDefault INTEGER NOT NULL DEFAULT 0,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                lastModified INTEGER NOT NULL,
                categoryType TEXT NOT NULL DEFAULT 'Expense'
            )
            """.trimIndent()
        )

        // Import Utils for icon mapping
        val utils = ke.ac.ku.ledgerly.utils.Utils

        // Insert default expense categories with icons
        val defaultExpenseCategories = listOf(
            Pair("grocery", Pair("Grocery", -11935381)),
            Pair("netflix", Pair("Netflix", -52480)),
            Pair("rent", Pair("Rent", -4207945)),
            Pair("paypal", Pair("Paypal", -16776961)),
            Pair("starbucks", Pair("Starbucks", -8410369)),
            Pair("shopping", Pair("Shopping", -12189568)),
            Pair("transport", Pair("Transport", -6710887)),
            Pair("utilities", Pair("Utilities", -4147200)),
            Pair("dining_out", Pair("Dining Out", -13395456)),
            Pair("entertainment", Pair("Entertainment", -61681)),
            Pair("healthcare", Pair("Healthcare", -8847360)),
            Pair("insurance", Pair("Insurance", -1744830)),
            Pair("subscriptions", Pair("Subscriptions", -3670016)),
            Pair("education", Pair("Education", -5317953)),
            Pair("debt_payments", Pair("Debt Payments", -2236962)),
            Pair("gifts_donations", Pair("Gifts & Donations", -1275068)),
            Pair("travel", Pair("Travel", -12087627)),
            Pair("other_expenses", Pair("Other Expenses", -3355444))
        )

        for ((id, pair) in defaultExpenseCategories) {
            val (name, color) = pair
            val icon = utils.getItemIcon(name)
            db.execSQL(
                """
                INSERT INTO categories (id, name, icon, color, isDefault, categoryType, lastModified)
                VALUES (?, ?, ?, ?, 1, 'Expense', ?)
                """,
                arrayOf<Any>(id, name, icon, color, System.currentTimeMillis())
            )
        }

        // Insert default income categories with icons
        val defaultIncomeCategories = listOf(
            Pair("salary", Pair("Salary", -3713642)),
            Pair("freelance", Pair("Freelance", -14575885)),
            Pair("investments", Pair("Investments", -8454016)),
            Pair("bonus", Pair("Bonus", -4725256)),
            Pair("rental_income", Pair("Rental Income", -10702155)),
            Pair("other_income", Pair("Other Income", -3355444))
        )

        for ((id, pair) in defaultIncomeCategories) {
            val (name, color) = pair
            val icon = utils.getItemIcon(name)
            db.execSQL(
                """
                INSERT INTO categories (id, name, icon, color, isDefault, categoryType, lastModified)
                VALUES (?, ?, ?, ?, 1, 'Income', ?)
                """,
                arrayOf<Any>(id, name, icon, color, System.currentTimeMillis())
            )
        }
    }
}


