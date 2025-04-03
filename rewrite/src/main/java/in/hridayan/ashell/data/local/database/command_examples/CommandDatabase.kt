package `in`.hridayan.ashell.data.local.database.command_examples

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.data.local.database.Converters
import `in`.hridayan.ashell.data.local.model.command_examples.CommandEntity
import `in`.hridayan.ashell.data.local.model.command_examples.PredefinedCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.InputStreamReader

@Database(entities = [CommandEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CommandDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao

    companion object {
        @Volatile
        private var INSTANCE: CommandDatabase? = null
        fun getDatabase(context: Context, scope: CoroutineScope): CommandDatabase {
            Log.d("Database", "getDatabase() called")

            return INSTANCE ?: synchronized(this) {
                Log.d("Database", "Creating new database instance")

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CommandDatabase::class.java,
                    "command_database"
                )
                    .addTypeConverter(Converters())
                    .addCallback(CommandDatabaseCallback(context, scope))
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private class CommandDatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d("Database", "onCreate() called")

                scope.launch {
                    Log.d("Database", "⚡ Populating database")
                    val dao = getDatabase(context, scope)
                        .commandDao()
                    populateDatabase(context, dao)
                    Log.d("Database", "Database population completed")
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d("Database", "onOpen() called")
            }

            private suspend fun populateDatabase(context: Context, commandDao: CommandDao) {
                val predefinedCommands = loadCommandsFromJson(context)
                val count = commandDao.getCommandCount()

                if (count == 0) {
                    val commandsToInsert = predefinedCommands.map { predefined ->
                        CommandEntity(
                            command = predefined.command,
                            description = predefined.description,
                            example = predefined.example,
                            isFavourite = false,
                            labels = emptyList(),
                            useCount = 0
                        )
                    }

                    commandDao.insertPredefinedCommands(commandsToInsert)
                    Log.d("Database", "Predefined commands inserted!")
                } else {
                    Log.d("Database", "Predefined commands already exist, skipping insertion.")
                }
            }

            private fun loadCommandsFromJson(context: Context): List<PredefinedCommand> {
                val inputStream = context.resources.openRawResource(R.raw.commands)
                val reader = InputStreamReader(inputStream)
                val type = object : TypeToken<List<PredefinedCommand>>() {}.type
                return Gson().fromJson(reader, type)
            }
        }
    }
}
