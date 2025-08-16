package `in`.hridayan.ashell.core.domain.repository

import `in`.hridayan.ashell.core.data.model.BookmarkEntity
import `in`.hridayan.ashell.core.domain.model.SortType
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    suspend fun addBookmark(command: String)
    suspend fun deleteBookmarkByCommand(command: String)
    suspend fun deleteAllBookmarks()
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>
     fun isBookmarked(command: String): Flow<Boolean>
    fun getBookmarkCount(): Flow<Int>
    fun getBookmarksSorted(sortType: SortType): Flow<List<BookmarkEntity>>
}
