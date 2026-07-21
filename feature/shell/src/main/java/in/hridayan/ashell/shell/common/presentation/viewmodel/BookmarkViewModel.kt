package `in`.hridayan.ashell.shell.common.presentation.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.shell.common.data.model.BookmarkEntity
import `in`.hridayan.ashell.shell.common.domain.repository.BookmarkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Stable
@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val repository: BookmarkRepository
) : ViewModel() {

    private val _bookmarks = mutableStateOf<List<BookmarkEntity>>(emptyList())
    val bookmarks: State<List<BookmarkEntity>> = _bookmarks

    private val _sortType = MutableStateFlow(SortType.AZ)

    private val _bookmarksSearchQuery = MutableStateFlow(TextFieldValue(""))
    val bookmarksSearchQuery: StateFlow<TextFieldValue> = _bookmarksSearchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _sortedBookmarks = _sortType
        .flatMapLatest { sortType ->
            repository.getSortedBookmarksFlow(sortType)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val searchedBookmarks = combine(
        _bookmarksSearchQuery,
        _sortedBookmarks
    ) { query, bookmarks ->

        withContext(Dispatchers.Default) {
            var filtered = bookmarks

            if (query.text.isNotBlank()) {
                filtered = filtered.filter {
                    it.command.contains(query.text, ignoreCase = true)
                }
            }

            filtered
        }
    }.distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun onSearchQueryChange(newValue: TextFieldValue) {
        _bookmarksSearchQuery.value = newValue
    }

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

    fun setSortType(sortType: Int) {
        _sortType.value = sortType
    }
}
