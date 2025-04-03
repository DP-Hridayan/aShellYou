package `in`.hridayan.ashell.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import `in`.hridayan.ashell.data.local.database.command_examples.CommandDao
import `in`.hridayan.ashell.data.local.database.command_examples.LabelDao
import `in`.hridayan.ashell.data.local.model.command_examples.CommandEntity
import `in`.hridayan.ashell.data.local.model.command_examples.LabelEntity
import kotlinx.coroutines.CoroutineScope

@Database(entities = [CommandEntity::class, LabelEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
    abstract fun labelDao(): LabelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "command_examples_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE commandExamples ADD COLUMN position INTEGER DEFAULT 0 NOT NULL")
            }
        }
    }
}




