package `in`.hridayan.ashell.core.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.domain.repository.GithubDataRepository
import `in`.hridayan.ashell.core.utils.isNetworkAvailable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GithubDataViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: GithubDataRepository
) : ViewModel() {

    val stats = repository.observeRepoStats()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )

    init {
        refreshIfPossible()
    }

    fun refreshIfPossible() {
        if (isNetworkAvailable(context)) {
            viewModelScope.launch {
                repository.refreshRepoStats()
            }
        }
    }
}
