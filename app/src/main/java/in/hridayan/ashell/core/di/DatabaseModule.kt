package `in`.hridayan.ashell.core.di

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
import `in`.hridayan.ashell.commandexamples.data.local.preloadedCommands
import `in`.hridayan.ashell.core.common.converters.StringListConverter
import `in`.hridayan.ashell.core.data.database.BookmarkDao
import `in`.hridayan.ashell.core.data.database.BookmarkDatabase
import `in`.hridayan.ashell.crashreporter.data.database.CrashDatabase
import `in`.hridayan.ashell.crashreporter.data.database.CrashLogDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CommandDatabase {
        return Room.databaseBuilder(
            context,
            CommandDatabase::class.java,
            "command_database"
        )
            .addTypeConverter(StringListConverter())
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        val database = Room.databaseBuilder(
                            context,
                            CommandDatabase::class.java,
                            "command_database"
                        )
                            .addTypeConverter(StringListConverter())
                            .build()

                        database.commandDao().insertAllCommands(preloadedCommands)
                    }
                }
            })
            .fallbackToDestructiveMigration(false)
            .build()
    }


    @Provides
    fun provideCommandDao(database: CommandDatabase): CommandDao {
        return database.commandDao()
    }

    @Provides
    @Singleton
    fun provideBookmarkDatabase(@ApplicationContext context: Context): BookmarkDatabase {
        return Room.databaseBuilder(
            context,
            BookmarkDatabase::class.java,
            "bookmark_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideBookmarkDao(database: BookmarkDatabase): BookmarkDao {
        return database.bookmarkDao()
    }

    @Provides
    @Singleton
    fun provideCrashDatabase(
        @ApplicationContext context: Context
    ): CrashDatabase {
        return Room.databaseBuilder(
            context,
            CrashDatabase::class.java,
            "crash_database"
        ).fallbackToDestructiveMigration(dropAllTables = false)
            .build()
    }

    @Provides
    fun provideCrashLogDao(db: CrashDatabase): CrashLogDao {
        return db.crashLogDao()
    }
}