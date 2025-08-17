package `in`.hridayan.ashell.core.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.data.model.BookmarkEntity
import `in`.hridayan.ashell.core.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val repository: BookmarkRepository
) : ViewModel() {

    private val _bookmarks = mutableStateOf<List<BookmarkEntity>>(emptyList())
    val bookmarks: State<List<BookmarkEntity>> = _bookmarks

    fun getAllBookmarks(sortType: Int): State<List<BookmarkEntity>> {
        preloadBookmarks(sortType)
        return _bookmarks
    }

    private fun preloadBookmarks(sortType: Int) {
        viewModelScope.launch {
            _bookmarks.value = repository.getBookmarksSorted(sortType)
        }
    }

    fun addBookmark(command: String) = viewModelScope.launch {
        repository.addBookmark(command.trim())
    }

    fun deleteBookmark(command: String) = viewModelScope.launch {
        repository.deleteBookmarkByCommand(command.trim())
    }

    fun deleteAllBookmark() = viewModelScope.launch {
        repository.deleteAllBookmarks()
    }

    fun isBookmarked(command: String): Flow<Boolean> {
        return repository.isBookmarked(command.trim())
    }

    val getBookmarkCount = repository.getBookmarkCount()
}
