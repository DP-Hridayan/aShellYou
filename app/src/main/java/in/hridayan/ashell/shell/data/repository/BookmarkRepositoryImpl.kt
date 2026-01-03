package `in`.hridayan.ashell.shell.data.repository

import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.shell.domain.repository.BookmarkRepository
import `in`.hridayan.ashell.shell.data.database.BookmarkDao
import `in`.hridayan.ashell.shell.data.model.BookmarkEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val dao: BookmarkDao
) : BookmarkRepository {

    override suspend fun addBookmark(command: String) {
        dao.addBookmark(BookmarkEntity(command = command))
    }

    override suspend fun deleteBookmarkByCommand(command: String) {
        dao.deleteBookmarkByCommand(command)
    }

    override suspend fun deleteAllBookmarks() {
        dao.deleteAllBookmarks()
    }

    override fun isBookmarked(command: String): Flow<Boolean> {
        return dao.isBookmarked(command)
    }

    override fun getBookmarkCount(): Flow<Int> {
        return dao.getBookmarkCount()
    }

    override suspend fun getBookmarksSorted(sortType: Int): List<BookmarkEntity> {
        return when (sortType) {
            SortType.AZ -> dao.getBookmarksSortedAZ()
            SortType.ZA -> dao.getBookmarksSortedZA()
            SortType.NEWEST -> dao.getBookmarksSortedNewest()
            SortType.OLDEST -> dao.getBookmarksSortedOldest()
            else -> dao.getBookmarksSortedAZ()
        }
    }
}