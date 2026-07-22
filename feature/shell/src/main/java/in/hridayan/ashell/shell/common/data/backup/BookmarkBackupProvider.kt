package `in`.hridayan.ashell.shell.common.data.backup

import `in`.hridayan.ashell.core.common.domain.backup.BackupProvider
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.shell.common.data.model.BookmarkEntity
import `in`.hridayan.ashell.shell.common.domain.repository.BookmarkRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import javax.inject.Inject

class BookmarkBackupProvider @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val json: Json
) : BackupProvider {

    override val featureId: String = "bookmarks"

    override suspend fun getBackupData(): JsonElement? {
        val bookmarks = bookmarkRepository.getBookmarksSorted(SortType.AZ)
        if (bookmarks.isEmpty()) return null
        return json.encodeToJsonElement(bookmarks)
    }

    override suspend fun restoreData(data: JsonElement?, legacyData: (String) -> JsonElement?) {
        val jsonData = data ?: legacyData("bookmarks") ?: return

        try {
            val bookmarks = json.decodeFromJsonElement<List<BookmarkEntity>>(jsonData)
            if (bookmarks.isNotEmpty()) {
                bookmarkRepository.deleteAllBookmarks()
                bookmarkRepository.insertAllBookmarks(bookmarks)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
