package `in`.hridayan.ashell.qstiles.presentation.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.qstiles.data.model.TileIcon
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.data.repository.TileRepositoryImpl
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
    private val repositoryImpl: TileRepositoryImpl,
    private val keywordProcessor: TileCommandKeywordProcessor
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTileState())
    val state: StateFlow<CreateTileState> = _state.asStateFlow()

    private val _iconsList = MutableStateFlow(TileIconProvider.icons)
    val iconsList: StateFlow<List<TileIcon>> = _iconsList.asStateFlow()

    init {
        observeSlots()
    }

    private fun observeSlots() {
        viewModelScope.launch {
            repository.getTiles().collect { tiles ->
                val activeCount = tiles.count { it.isActive }
                _state.update {
                    it.copy(
                        availableSlots = 10 - activeCount,
                        isFull = activeCount >= 10
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

    fun onIconQueryChange(query: TextFieldValue) {
        _state.update { it.copy(iconSearchQuery = query) }
        _iconsList.update {
            TileIconProvider.icons.filter { icon ->
                icon.keywords.any { it.contains(query.text, ignoreCase = true) }
            }
        }
    }

    private fun suggestIcons(command: String) {
        val keywords = keywordProcessor.extractKeywords(command)
        val scored = mutableMapOf<String, Int>()
        keywords.forEach { keyword ->
            TileIconProvider.iconsByKeyword[keyword]?.forEach { icon ->
                scored[icon.id] = (scored[icon.id] ?: 0) + 1
            }
        }
        val topIcons = scored.toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
        _state.update { it.copy(suggestedIcons = topIcons) }
    }

    fun createTile() {
        viewModelScope.launch {
            val allTiles = repository.getTiles().first()
            // ID is unconstrained — use next integer above all existing IDs
            val nextId = (allTiles.maxOfOrNull { it.id } ?: 0) + 1

            val s = _state.value

            val tile = TileConfig(
                id = nextId,
                name = s.name.ifBlank { "Tile $nextId" },
                command = s.command,
                executionMode = s.executionMode,
                iconId = s.selectedIconId,
                isActive = false,  // Slot assignment handles activation
                isCustom = true,
                slotIndex = null,
                timeoutMs = null   // No timeout by default
            )

            // Persist then activate (assigns the first free slot atomically)
            repository.createTile(tile)
            repositoryImpl.activateTile(tile)
        }
    }
}
