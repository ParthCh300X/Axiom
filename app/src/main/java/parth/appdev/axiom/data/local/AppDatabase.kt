package parth.appdev.axiom.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import parth.appdev.axiom.data.local.dao.CategoryDao
import parth.appdev.axiom.data.local.dao.TransactionDao
import parth.appdev.axiom.data.local.entity.CategoryEntity
import parth.appdev.axiom.data.local.entity.TransactionEntity

@Database(
    entities = [CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "axiom_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}