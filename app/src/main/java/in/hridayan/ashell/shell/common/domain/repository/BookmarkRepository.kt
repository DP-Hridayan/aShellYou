package `in`.hridayan.ashell.shell.common.domain.repository

import `in`.hridayan.ashell.shell.common.data.model.BookmarkEntity
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    suspend fun addBookmark(command: String)
    suspend fun deleteBookmarkByCommand(command: String)
    suspend fun deleteAllBookmarks()
    suspend fun insertAllBookmarks(bookmarks: List<BookmarkEntity>)
    suspend fun getBookmarksSorted(sortType: Int): List<BookmarkEntity>
    fun getSortedBookmarksFlow(sortType: Int): Flow<List<BookmarkEntity>>
    fun isBookmarked(command: String): Flow<Boolean>
    fun getBookmarkCount(): Flow<Int>
}
