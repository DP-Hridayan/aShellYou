package `in`.hridayan.ashell.qstiles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import `in`.hridayan.ashell.qstiles.domain.usecase.GetSuggestedIconsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TileViewModel @Inject constructor(
    private val suggestIcons: GetSuggestedIconsUseCase,
    private val repository: TileRepository
) : ViewModel() {

    private val _suggestedIcons = MutableStateFlow<List<String>>(emptyList())
    val suggestedIcons: StateFlow<List<String>> = _suggestedIcons

    fun onCommandChanged(command: String) {
        _suggestedIcons.value = suggestIcons(command)
    }

    fun saveTile(config: TileConfig) {
        viewModelScope.launch {
            repository.saveTile(config)
        }
    }
}