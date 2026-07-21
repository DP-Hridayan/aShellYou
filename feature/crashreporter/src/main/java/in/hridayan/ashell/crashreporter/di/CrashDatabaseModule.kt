package `in`.hridayan.ashell.crashreporter.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.crashreporter.data.database.CrashDatabase
import `in`.hridayan.ashell.crashreporter.data.database.CrashLogDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CrashDatabaseModule {

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
