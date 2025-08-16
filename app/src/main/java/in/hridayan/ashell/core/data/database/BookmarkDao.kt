package `in`.hridayan.ashell.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import `in`.hridayan.ashell.core.data.model.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE command = :command")
    suspend fun deleteBookmarkByCommand(command: String)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE command = :command)")
    fun isBookmarked(command: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM bookmarks")
    fun getBookmarkCount(): Flow<Int>

    @Query("SELECT * FROM bookmarks ORDER BY command ASC")
    suspend fun getBookmarksSortedAZ(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks ORDER BY command DESC")
    suspend fun getBookmarksSortedZA(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks ORDER BY id DESC")
    suspend fun getBookmarksSortedNewest(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks ORDER BY id ASC")
    suspend fun getBookmarksSortedOldest(): List<BookmarkEntity>
}
