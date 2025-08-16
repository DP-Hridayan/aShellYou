package `in`.hridayan.ashell.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.data.model.BookmarkEntity
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.core.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val repository: BookmarkRepository
) : ViewModel() {

    val bookmarks = repository.getAllBookmarks().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    fun addBookmark(command: String) = viewModelScope.launch {
        repository.addBookmark(command)
    }

    fun deleteBookmark(command: String) = viewModelScope.launch {
        repository.deleteBookmarkByCommand(command)
    }

    fun deleteAllBookmark() = viewModelScope.launch {
        repository.deleteAllBookmarks()
    }

    fun sortBookmark(sortType: SortType): Flow<List<BookmarkEntity>> {
        return repository.getBookmarksSorted(sortType)
    }

    fun isBookmarked(command: String): Flow<Boolean> {
        return repository.isBookmarked(command)
    }

    val getBookmarkCount = repository.getBookmarkCount()
}
