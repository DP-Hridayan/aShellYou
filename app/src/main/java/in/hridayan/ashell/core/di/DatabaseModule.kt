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
import `in`.hridayan.ashell.core.data.database.BookmarkDao
import `in`.hridayan.ashell.core.data.database.BookmarkDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CommandDatabase {
        return Room.databaseBuilder(
            context, CommandDatabase::class.java, "command_database"
        )
            .addTypeConverter(StringListConverter())
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
}