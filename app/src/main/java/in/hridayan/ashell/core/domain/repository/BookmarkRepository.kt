package `in`.hridayan.ashell.core.domain.repository

import `in`.hridayan.ashell.core.data.model.BookmarkEntity
import `in`.hridayan.ashell.core.domain.model.SortType
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    suspend fun addBookmark(command: String)
    suspend fun deleteBookmarkByCommand(command: String)
    suspend fun deleteAllBookmarks()
    suspend fun getBookmarksSorted(sortType: Int): List<BookmarkEntity>
    fun isBookmarked(command: String): Flow<Boolean>
    fun getBookmarkCount(): Flow<Int>
}
