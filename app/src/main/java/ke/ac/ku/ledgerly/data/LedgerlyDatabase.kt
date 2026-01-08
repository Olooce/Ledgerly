package ke.ac.ku.ledgerly.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.dao.BillReminderDao
import ke.ac.ku.ledgerly.data.dao.BudgetDao
import ke.ac.ku.ledgerly.data.dao.CategoryDao
import ke.ac.ku.ledgerly.data.dao.DebtDao
import ke.ac.ku.ledgerly.data.dao.NotificationDao
import ke.ac.ku.ledgerly.data.dao.RecurringTransactionDao
import ke.ac.ku.ledgerly.data.dao.SavingsGoalDao
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.BillReminderEntity
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.data.model.CategoryEntity
import ke.ac.ku.ledgerly.data.model.DebtEntity
import ke.ac.ku.ledgerly.data.model.NotificationEntity
import ke.ac.ku.ledgerly.data.model.RecurringTransactionEntity
import ke.ac.ku.ledgerly.data.model.SavingsGoalEntity
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.data.model.ExchangeRateEntity
import ke.ac.ku.ledgerly.data.converters.Converters
import ke.ac.ku.ledgerly.data.dao.ExchangeRateDao
import javax.inject.Singleton

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        RecurringTransactionEntity::class,
        CategoryEntity::class,
        DebtEntity::class,
        SavingsGoalEntity::class,
        BillReminderEntity::class,
        NotificationEntity::class,
        ExchangeRateEntity::class
    ],
    version = 19,
    exportSchema = false
)
@TypeConverters(Converters::class)
@Singleton
abstract class LedgerlyDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
    abstract fun debtDao(): DebtDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun billReminderDao(): BillReminderDao
    abstract fun notificationDao(): NotificationDao
    abstract fun exchangeRateDao(): ExchangeRateDao


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
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
                        MIGRATION_16_17,
                        MIGRATION_17_18,
                        MIGRATION_18_19

                    )
//                    .fallbackToDestructiveMigration() //  Delete and recreate the database: For Dev
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

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS debts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                personName TEXT NOT NULL,
                amount REAL NOT NULL,
                currency TEXT NOT NULL DEFAULT 'KES',
                debtType TEXT NOT NULL,
                dueDate INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'pending',
                description TEXT NOT NULL DEFAULT '',
                reminderDays INTEGER NOT NULL DEFAULT 0,
                reminderEnabled INTEGER NOT NULL DEFAULT 1,
                notes TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL,
                lastModified INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS savings_goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                targetAmount REAL NOT NULL,
                currentAmount REAL NOT NULL DEFAULT 0.0,
                icon TEXT NOT NULL DEFAULT '🎯',
                color TEXT NOT NULL DEFAULT '#4CAF50',
                targetDate INTEGER,
                createdDate INTEGER NOT NULL,
                lastModified INTEGER NOT NULL,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                isDeleted INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // 1. Add new temp column
        db.execSQL(
            "ALTER TABLE savings_goals ADD COLUMN icon_res INTEGER NOT NULL DEFAULT ${R.drawable.ic_target}"
        )

        // 2. Map old emoji values to drawable icons
        db.execSQL(
            """
            UPDATE savings_goals SET icon_res = CASE icon
                WHEN '🎯' THEN ${R.drawable.ic_target}
                WHEN '🏠' THEN ${R.drawable.ic_goal_house}
                WHEN '🚗' THEN ${R.drawable.ic_goal_car}
                WHEN '💻' THEN ${R.drawable.ic_goal_laptop}
                WHEN '✈️' THEN ${R.drawable.ic_goal_plane}
                WHEN '👨‍👩‍👧‍👦' THEN ${R.drawable.ic_goal_family}
                WHEN '🎓' THEN ${R.drawable.ic_goal_school}
                ELSE ${R.drawable.ic_target}
            END
            """.trimIndent()
        )

//        database.execSQL(
//            """
//            UPDATE savings_goals SET icon_res = CASE icon
//                WHEN '🎯' THEN ${R.drawable.ic_target}
//                WHEN '🏠' THEN ${R.drawable.ic_goal_house}
//                WHEN '🚗' THEN ${R.drawable.ic_goal_car}
//                WHEN '💻' THEN ${R.drawable.ic_goal_laptop}
//                WHEN '✈️' THEN ${R.drawable.ic_goal_plane}
//                WHEN '👨‍👩‍👧‍👦' THEN ${R.drawable.ic_goal_family}
//                WHEN '🎓' THEN ${R.drawable.ic_goal_school}
//                WHEN '💍' THEN ${R.drawable.ic_goal_ring}
//                WHEN '⌚' THEN ${R.drawable.ic_goal_watch}
//                WHEN '🎮' THEN ${R.drawable.ic_goal_game}
//                ELSE ${R.drawable.ic_target}
//            END
//            ""\"
//        )

        // 3. Recreate table WITHOUT old icon column
        db.execSQL(
            """
         CREATE TABLE savings_goals_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                targetAmount REAL NOT NULL,
                currentAmount REAL NOT NULL,
                icon INTEGER NOT NULL,
                color TEXT NOT NULL,
                targetDate INTEGER,
                createdDate INTEGER NOT NULL,
                lastModified INTEGER NOT NULL,
                isCompleted INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
               INSERT INTO savings_goals_new (
                    id, name, description, targetAmount, currentAmount,
                    icon, color, targetDate, createdDate, lastModified, isCompleted, isDeleted
                )
                SELECT
                    id, name, description, targetAmount, currentAmount,
                    icon_res, color, targetDate, createdDate, lastModified, isCompleted, isDeleted
                FROM savings_goals

            """.trimIndent()
        )

        db.execSQL("DROP TABLE savings_goals")
        db.execSQL("ALTER TABLE savings_goals_new RENAME TO savings_goals")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE bill_reminders_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                billName TEXT NOT NULL,
                description TEXT NOT NULL,
                amount REAL NOT NULL,
                currency TEXT NOT NULL,
                dueDate INTEGER NOT NULL,
                category TEXT NOT NULL,
                frequency TEXT NOT NULL,
                nextDueDate INTEGER,
                reminderDays INTEGER NOT NULL,
                reminderEnabled INTEGER NOT NULL,
                notificationSent INTEGER NOT NULL,
                isOverdue INTEGER NOT NULL,
                status TEXT NOT NULL,
                paymentMethod TEXT NOT NULL,
                notes TEXT NOT NULL,
                color INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                lastModified INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            "CREATE INDEX index_bill_reminders_status ON bill_reminders_new(status)"
        )
        db.execSQL(
            "CREATE INDEX index_bill_reminders_dueDate ON bill_reminders_new(dueDate)"
        )
        db.execSQL(
            "CREATE INDEX index_bill_reminders_isDeleted ON bill_reminders_new(isDeleted)"
        )

        db.execSQL("DROP TABLE IF EXISTS bill_reminders")
        db.execSQL("ALTER TABLE bill_reminders_new RENAME TO bill_reminders")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS notifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                relatedId INTEGER,
                relatedType TEXT,
                isRead INTEGER NOT NULL DEFAULT 0,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                createdDate INTEGER NOT NULL,
                readDate INTEGER,
                lastModified INTEGER NOT NULL,
                icon INTEGER,
                actionUrl TEXT
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_isRead ON notifications(isRead)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_createdDate ON notifications(createdDate)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_isDeleted ON notifications(isDeleted)")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            UPDATE savings_goals
            SET icon = ${R.drawable.ic_target}
            WHERE icon NOT IN (
                ${R.drawable.ic_target},
                ${R.drawable.ic_goal_house},
                ${R.drawable.ic_goal_car},
                ${R.drawable.ic_goal_laptop},
                ${R.drawable.ic_goal_plane},
                ${R.drawable.ic_goal_school}
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {


        db.execSQL(
            """
            CREATE TABLE notifications_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                relatedId TEXT,                -- ✅ FIXED
                relatedType TEXT,
                isRead INTEGER NOT NULL DEFAULT 0,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                createdDate INTEGER NOT NULL,
                readDate INTEGER,
                lastModified INTEGER NOT NULL,
                icon INTEGER,
                actionUrl TEXT
            )
            """.trimIndent()
        )


        db.execSQL(
            """
            INSERT INTO notifications_new (
                id, type, title, message, relatedId, relatedType,
                isRead, isDeleted, createdDate, readDate,
                lastModified, icon, actionUrl
            )
            SELECT
                id, type, title, message,
                CASE
                    WHEN relatedId IS NULL THEN NULL
                    ELSE relatedId || ''
                END,
                relatedType,
                isRead, isDeleted, createdDate, readDate,
                lastModified, icon, actionUrl
            FROM notifications
            """.trimIndent()
        )


        db.execSQL("DROP TABLE notifications")

        db.execSQL("ALTER TABLE notifications_new RENAME TO notifications")


        db.execSQL("CREATE INDEX index_notifications_isRead ON notifications(isRead)")
        db.execSQL("CREATE INDEX index_notifications_createdDate ON notifications(createdDate)")
        db.execSQL("CREATE INDEX index_notifications_isDeleted ON notifications(isDeleted)")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE savings_goals
            ADD COLUMN lastMilestoneReached REAL NOT NULL DEFAULT 0.0
            """.trimIndent()
        )
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE debts ADD COLUMN lastReminderSent INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                amountOriginal TEXT NOT NULL,
                originalCurrency TEXT NOT NULL,
                exchangeRateToUsd TEXT NOT NULL,
                amountUsd TEXT NOT NULL,
                date INTEGER NOT NULL,
                type TEXT NOT NULL,
                notes TEXT NOT NULL,
                paymentMethod TEXT NOT NULL,
                tags TEXT NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                lastModified INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO transactions_new (
                id, category, amountOriginal, originalCurrency, 
                exchangeRateToUsd, amountUsd, date, type, notes, 
                paymentMethod, tags, isDeleted, lastModified
            )
            SELECT 
                id, category, 
                CAST(amount AS TEXT),
                'KES',
                '1.0',
                CAST(amount AS TEXT),
                date, type, notes, 
                paymentMethod, tags, isDeleted, lastModified
            FROM transactions
        """.trimIndent())

        db.execSQL("DROP TABLE transactions")
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        db.execSQL("""
            CREATE TABLE recurring_transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                amountOriginal TEXT NOT NULL,
                originalCurrency TEXT NOT NULL,
                exchangeRateToUsd TEXT NOT NULL,
                amountUsd TEXT NOT NULL,
                type TEXT NOT NULL,
                notes TEXT NOT NULL,
                paymentMethod TEXT NOT NULL,
                tags TEXT NOT NULL,
                frequency TEXT NOT NULL,
                startDate INTEGER NOT NULL,
                endDate INTEGER,
                lastGeneratedDate INTEGER,
                isActive INTEGER NOT NULL DEFAULT 1,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                lastModified INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO recurring_transactions_new (
                id, category, amountOriginal, originalCurrency, 
                exchangeRateToUsd, amountUsd, type, notes, 
                paymentMethod, tags, frequency, startDate, endDate, 
                lastGeneratedDate, isActive, isDeleted, lastModified
            )
            SELECT 
                id, category,
                CAST(amount AS TEXT),
                'KES',
                '1.0',
                CAST(amount AS TEXT),
                type, notes, 
                paymentMethod, tags, frequency, startDate, endDate, 
                lastGeneratedDate, isActive, isDeleted, lastModified
            FROM recurring_transactions
        """.trimIndent())

        db.execSQL("DROP TABLE recurring_transactions")
        db.execSQL("ALTER TABLE recurring_transactions_new RENAME TO recurring_transactions")

        db.execSQL("""
            CREATE TABLE budgets_new (
                category TEXT NOT NULL,
                monthlyBudget TEXT NOT NULL,
                currentSpending TEXT NOT NULL DEFAULT '0',
                monthYear TEXT NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                lastModified INTEGER,
                PRIMARY KEY (category, monthYear)
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO budgets_new (
                category, monthlyBudget, currentSpending, 
                monthYear, isDeleted, lastModified
            )
            SELECT 
                category,
                CAST(monthlyBudget AS TEXT),
                CAST(currentSpending AS TEXT),
                monthYear, isDeleted, lastModified
            FROM budgets
        """.trimIndent())

        db.execSQL("DROP TABLE budgets")
        db.execSQL("ALTER TABLE budgets_new RENAME TO budgets")

        db.execSQL("""
            CREATE TABLE debts_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                personName TEXT NOT NULL,
                amountOriginal TEXT NOT NULL,
                originalCurrency TEXT NOT NULL,
                exchangeRateToUsd TEXT NOT NULL,
                amount TEXT NOT NULL,
                debtType TEXT NOT NULL,
                dueDate INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'pending',
                description TEXT NOT NULL DEFAULT '',
                reminderDays INTEGER NOT NULL DEFAULT 0,
                reminderEnabled INTEGER NOT NULL DEFAULT 1,
                reminderSent INTEGER NOT NULL DEFAULT 0,
                notes TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL,
                lastModified INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                lastReminderSent INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO debts_new (
                id, personName, amountOriginal, originalCurrency,
                exchangeRateToUsd, amount, debtType, dueDate, status,
                description, reminderDays, reminderEnabled, reminderSent,
                notes, createdAt, lastModified, isDeleted, lastReminderSent
            )
            SELECT 
                id, personName,
                CAST(amount AS TEXT),
                COALESCE(currency, 'KES'),
                '1.0',
                CAST(amount AS TEXT),
                debtType, dueDate, status,
                description, reminderDays, reminderEnabled, 0,
                notes, createdAt, lastModified, isDeleted, lastReminderSent
            FROM debts
        """.trimIndent())

        db.execSQL("DROP TABLE debts")
        db.execSQL("ALTER TABLE debts_new RENAME TO debts")

        // Bill reminders already have amount as REAL, just need to add currency if needed
        db.execSQL("""
            CREATE TABLE bill_reminders_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                billName TEXT NOT NULL,
                description TEXT NOT NULL,
                amount REAL NOT NULL,
                currency TEXT NOT NULL DEFAULT 'KES',
                dueDate INTEGER NOT NULL,
                category TEXT NOT NULL,
                frequency TEXT NOT NULL,
                nextDueDate INTEGER,
                reminderDays INTEGER NOT NULL,
                reminderEnabled INTEGER NOT NULL,
                notificationSent INTEGER NOT NULL,
                isOverdue INTEGER NOT NULL,
                status TEXT NOT NULL,
                paymentMethod TEXT NOT NULL,
                notes TEXT NOT NULL,
                color INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                lastModified INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO bill_reminders_new (
                id, billName, description, amount, currency,
                dueDate, category, frequency, nextDueDate,
                reminderDays, reminderEnabled, notificationSent,
                isOverdue, status, paymentMethod, notes, color,
                createdAt, lastModified, isDeleted
            )
            SELECT 
                id, billName, description, amount, 
                COALESCE(currency, 'KES'),
                dueDate, category, frequency, nextDueDate,
                reminderDays, reminderEnabled, notificationSent,
                isOverdue, status, paymentMethod, notes, color,
                createdAt, lastModified, isDeleted
            FROM bill_reminders
        """.trimIndent())

        db.execSQL("DROP TABLE bill_reminders")
        db.execSQL("ALTER TABLE bill_reminders_new RENAME TO bill_reminders")

        // Recreate indices for bill_reminders
        db.execSQL("CREATE INDEX index_bill_reminders_status ON bill_reminders(status)")
        db.execSQL("CREATE INDEX index_bill_reminders_dueDate ON bill_reminders(dueDate)")
        db.execSQL("CREATE INDEX index_bill_reminders_isDeleted ON bill_reminders(isDeleted)")


        db.execSQL("""
            CREATE TABLE savings_goals_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                targetAmount TEXT NOT NULL,
                currentAmount TEXT NOT NULL DEFAULT '0',
                icon INTEGER NOT NULL,
                color TEXT NOT NULL DEFAULT '#4CAF50',
                targetDate INTEGER,
                lastMilestoneReached TEXT NOT NULL DEFAULT '0',
                createdDate INTEGER NOT NULL,
                lastModified INTEGER NOT NULL,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                isDeleted INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO savings_goals_new (
                id, name, description, targetAmount, currentAmount,
                icon, color, targetDate, lastMilestoneReached,
                createdDate, lastModified, isCompleted, isDeleted
            )
            SELECT 
                id, name, description,
                CAST(targetAmount AS TEXT),
                CAST(currentAmount AS TEXT),
                icon, color, targetDate,
                CAST(COALESCE(lastMilestoneReached, 0.0) AS TEXT),
                createdDate, lastModified, isCompleted, isDeleted
            FROM savings_goals
        """.trimIndent())

        db.execSQL("DROP TABLE savings_goals")
        db.execSQL("ALTER TABLE savings_goals_new RENAME TO savings_goals")
    }
}


val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS exchange_rates (
                baseCurrency TEXT NOT NULL PRIMARY KEY,
                ratesJson TEXT NOT NULL,
                lastUpdated INTEGER NOT NULL
            )
            """
        )
    }
}





