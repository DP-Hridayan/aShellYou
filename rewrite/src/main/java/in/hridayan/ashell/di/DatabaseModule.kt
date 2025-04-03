package `in`.hridayan.ashell.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.data.local.database.AppDatabase
import `in`.hridayan.ashell.data.local.database.Converters
import `in`.hridayan.ashell.data.local.database.command_examples.CommandDao
import `in`.hridayan.ashell.data.local.database.command_examples.LabelDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        Log.d("DatabaseModule", "Creating database instance")
        return Room.databaseBuilder(
            context, AppDatabase::class.java, "command_examples_db"
        ).addTypeConverter(Converters()).addMigrations(AppDatabase.MIGRATION_1_2).build()
    }

    @Provides
    @Singleton
    fun provideCommandDao(database: AppDatabase): CommandDao {
        Log.d("DatabaseModule", "Providing CommandDao instance")
        return database.commandDao()
    }

    @Provides
    @Singleton
    fun provideLabelDao(database: AppDatabase): LabelDao {
        Log.d("DatabaseModule", "Providing LabelDao instance")
        return database.labelDao()
    }

}