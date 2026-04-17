package `in`.hridayan.ashell.qstiles.presentation.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.qstiles.data.model.TileIcon
import `in`.hridayan.ashell.qstiles.data.provider.TileComponentManager
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider.getIconRes
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.processor.TileCommandKeywordProcessor
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import `in`.hridayan.ashell.qstiles.presentation.model.CreateTileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTileViewModel @Inject constructor(
    private val repository: TileRepository,
    private val keywordProcessor: TileCommandKeywordProcessor,
    private val tileComponentManager: TileComponentManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<NavRoutes.CreateTileScreen>()

    private val _state = MutableStateFlow(CreateTileState(tileId = route.tileId))
    val state: StateFlow<CreateTileState> = _state.asStateFlow()

    private val _iconsList = MutableStateFlow(TileIconProvider.icons)
    val iconsList: StateFlow<List<TileIcon>> = _iconsList.asStateFlow()

    init {
        loadExistingTile()
    }

    private fun loadExistingTile() {
        viewModelScope.launch {
            repository.getTileOnce(route.tileId)?.let { config ->
                _state.update {
                    it.copy(
                        name = config.name,
                        command = config.command,
                        executionMode = config.executionMode,
                        selectedIconId = config.iconId,
                        isUpdateMode = true
                    )
                }
                suggestIcons(config.command)
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
            val s = _state.value

            val tile = TileConfig(
                id = s.tileId,
                name = s.name.ifBlank { "Untitled" },
                command = s.command,
                executionMode = s.executionMode,
                iconId = s.selectedIconId,
                isActive = true,
                isCustom = true,
                slotIndex = s.tileId - 1,
                timeoutMs = null
            )

            // Persist to specific slot
            repository.createTile(tile)

            // AUTO-PROMPT: Ask user to add/update in panel
            tileComponentManager.promptAddTile(
                tile.slotIndex!!,
                tile.name,
                getIconRes(tile.iconId)
            )
        }
    }

    fun deleteTile() {
        viewModelScope.launch {
            repository.deleteTile(route.tileId)
        }
    }
}


