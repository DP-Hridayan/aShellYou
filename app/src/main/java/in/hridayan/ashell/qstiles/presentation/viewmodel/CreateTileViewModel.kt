package `in`.hridayan.ashell.qstiles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.processor.TileCommandKeywordProcessor
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import `in`.hridayan.ashell.qstiles.presentation.model.CreateTileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTileViewModel @Inject constructor(
    private val repository: TileRepository,
    private val keywordProcessor: TileCommandKeywordProcessor
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTileState())
    val state: StateFlow<CreateTileState> = _state.asStateFlow()

    init {
        observeSlots()
    }

    private fun observeSlots() {
        viewModelScope.launch {
            repository.getTiles().collect { tiles ->
                val usedIds = tiles.map { it.id }
                val available = (1..10).filterNot { it in usedIds }

                _state.update {
                    it.copy(
                        availableSlots = available.size,
                        isFull = available.isEmpty()
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun onCommandChange(command: String) {
        _state.update { it.copy(command = command) }
        suggestIcons(command)
    }

    fun onExecutionModeChange(mode: Int) {
        _state.update { it.copy(executionMode = mode) }
    }

    fun onIconSelected(iconId: String) {
        _state.update { it.copy(selectedIconId = iconId) }
    }

    private fun suggestIcons(command: String) {
        val keywords = keywordProcessor.extractKeywords(command)

        val scored = mutableMapOf<String, Int>()

        keywords.forEach { keyword ->
            TileIconProvider.iconsByKeyword[keyword]?.forEach { icon ->
                scored[icon.id] = (scored[icon.id] ?: 0) + 1
            }
        }

        val topIcons = scored
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }

        _state.update {
            it.copy(suggestedIcons = topIcons)
        }
    }

    fun createTile() {
        viewModelScope.launch {
            val current = repository.getTiles().first()
            val usedIds = current.map { it.id }

            val nextId = (1..10).firstOrNull { it !in usedIds } ?: return@launch

            val s = _state.value

            val tile = TileConfig(
                id = nextId,
                name = s.name.ifBlank { "Tile $nextId" },
                command = s.command,
                executionMode = s.executionMode,
                iconId = s.selectedIconId,
                isActive = true,
                isCustom = true
            )

            repository.createTile(tile)
        }
    }
}