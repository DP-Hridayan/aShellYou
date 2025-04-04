package `in`.hridayan.ashell.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.data.local.database.AppDatabase
import `in`.hridayan.ashell.data.local.database.command_examples.CommandDao
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "command_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCommandDao(database: AppDatabase): CommandDao {
        return database.commandDao()
    }
}
