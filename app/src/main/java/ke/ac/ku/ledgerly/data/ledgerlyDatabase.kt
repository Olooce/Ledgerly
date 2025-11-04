package ke.ac.ku.ledgerly.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ke.ac.ku.ledgerly.data.dao.TransactionDao
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import javax.inject.Singleton

@Database(entities = [TransactionEntity::class, BudgetEntity::class], version = 3, exportSchema = false)
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
                    .addMigrations(MIGRATION_1_2)
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
            CREATE TABLE IF NOT EXISTS expense_table_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                type TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO expense_table_new (id, title, amount, date, type)
            SELECT id, title, amount, date, type FROM expense_table
            """.trimIndent()
        )

        db.execSQL("DROP TABLE expense_table")

        db.execSQL("ALTER TABLE expense_table_new RENAME TO expense_table")
    }
}