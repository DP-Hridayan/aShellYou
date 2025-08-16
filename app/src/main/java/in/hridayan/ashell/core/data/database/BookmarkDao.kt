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

    @Query("SELECT * FROM bookmarks")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE command = :command)")
     fun isBookmarked(command: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM bookmarks")
    fun getBookmarkCount(): Flow<Int>

    @Query("SELECT * FROM bookmarks ORDER BY command ASC")
    fun getBookmarksSortedAZ(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY command DESC")
    fun getBookmarksSortedZA(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY id DESC")
    fun getBookmarksSortedNewest(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY id ASC")
    fun getBookmarksSortedOldest(): Flow<List<BookmarkEntity>>
}
