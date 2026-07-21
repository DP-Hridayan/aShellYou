package `in`.hridayan.ashell.shell.common.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.ashell.shell.common.data.database.BookmarkDao
import `in`.hridayan.ashell.shell.common.data.database.BookmarkDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookmarkDatabaseModule {

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
