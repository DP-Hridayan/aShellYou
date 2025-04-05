package `in`.hridayan.ashell.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.commandexamples.data.local.database.CommandDao
import `in`.hridayan.ashell.commandexamples.data.local.database.CommandDatabase
import `in`.hridayan.ashell.core.common.converters.StringListConverter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CommandDatabase {
        return Room.databaseBuilder(
            context, CommandDatabase::class.java, "command_database"
        ).addTypeConverter(StringListConverter()).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideCommandDao(database: CommandDatabase): CommandDao {
        return database.commandDao()
    }
}