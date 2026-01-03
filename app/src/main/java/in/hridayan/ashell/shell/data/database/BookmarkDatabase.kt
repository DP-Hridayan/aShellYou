package `in`.hridayan.ashell.shell.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import `in`.hridayan.ashell.shell.data.model.BookmarkEntity

@Database(entities = [BookmarkEntity::class], version = 1, exportSchema = false)
abstract class BookmarkDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}