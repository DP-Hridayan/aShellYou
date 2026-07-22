package `in`.hridayan.ashell.commandexamples.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.commandexamples.data.local.database.CommandDao
import `in`.hridayan.ashell.commandexamples.data.local.database.CommandDatabase
import `in`.hridayan.ashell.commandexamples.data.local.source.preloadedCommands
import `in`.hridayan.ashell.core.common.converters.StringListConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommandDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CommandDatabase {
        lateinit var database: CommandDatabase

        database = Room.databaseBuilder(
            context,
            CommandDatabase::class.java,
            "command_database"
        )
            .addTypeConverter(StringListConverter())
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        database.commandDao().insertAllCommands(preloadedCommands)
                    }
                }
            })
            .fallbackToDestructiveMigration(false)
            .build()

        return database
    }

    @Provides
    fun provideCommandDao(database: CommandDatabase): CommandDao {
        return database.commandDao()
    }
}
