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
import `in`.hridayan.ashell.qstiles.domain.model.TileActiveState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.processor.TileCommandKeywordProcessor
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import `in`.hridayan.ashell.qstiles.presentation.model.CreateNewTileScreenUiState
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
    private val keywordProcessor: TileCommandKeywordProcessor,
    private val tileComponentManager: TileComponentManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<NavRoutes.CreateTileScreen>()

    private val _state = MutableStateFlow(CreateNewTileScreenUiState(tileId = route.tileId))
    val state: StateFlow<CreateNewTileScreenUiState> = _state.asStateFlow()

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
                        executionMode = config.executionMode,
                        selectedIconId = config.iconId,
                        isUpdateMode = true,
                        isToggleable = config.activeState.isToggleable,
                        isActive = config.activeState.isActive,
                        activeCommand = config.activeState.activeCommand,
                        inactiveCommand = config.activeState.inactiveCommand,
                        activeSubtitle = config.activeState.activeTileSubtitle,
                        inactiveSubtitle = config.activeState.inactiveTileSubtitle,
                    )
                }
                suggestIcons(config.activeState.activeCommand)
            }
        }
    }

    fun onNameChange(name: String) {
        viewModelScope.launch {
            val tiles = repository.getTiles().first()
            val isDuplicate = tiles.any {
                it.name.equals(name, ignoreCase = true) && it.id != route.tileId
            }
            _state.update {
                it.copy(
                    name = name,
                    nameError = if (isDuplicate) "A tile with this name already exists" else null
                )
            }
        }
    }

    fun onActiveCommandChange(command: String) {
        _state.update { it.copy(activeCommand = command) }
        suggestIcons(command)
    }

    fun onInactiveCommandChange(command: String) =
        _state.update { it.copy(inactiveCommand = command) }

    fun onExecutionModeChange(mode: Int) =
        _state.update { it.copy(executionMode = mode) }

    fun onIconSelected(iconId: String) =
        _state.update { it.copy(selectedIconId = iconId) }

    fun onIconQueryChange(query: TextFieldValue) {
        _state.update { it.copy(iconSearchQuery = query) }
        _iconsList.update {
            TileIconProvider.icons.filter { icon ->
                icon.keywords.any { it.contains(query.text, ignoreCase = true) }
            }
        }
    }

    fun onToggleableChange(isToggleable: Boolean) =
        _state.update { it.copy(isToggleable = isToggleable) }

    fun onActiveStateChange(isActive: Boolean) =
        _state.update { it.copy(isActive = isActive) }

    fun onActiveSubtitleChange(subtitle: String) =
        _state.update { it.copy(activeSubtitle = subtitle) }

    fun onInactiveSubtitleChange(subtitle: String) =
        _state.update { it.copy(inactiveSubtitle = subtitle) }

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

    /** Builds a [TileActiveState] from the current UI state snapshot. */
    private fun CreateNewTileScreenUiState.toActiveState() = TileActiveState(
        isToggleable = isToggleable,
        isActive = isActive,
        activeCommand = activeCommand,
        inactiveCommand = if (isToggleable) inactiveCommand else "",
        activeTileSubtitle = activeSubtitle,
        inactiveTileSubtitle = if (isToggleable) inactiveSubtitle else activeSubtitle,
    )

    fun createTile() {
        viewModelScope.launch {
            val s = _state.value

            val tile = TileConfig(
                id = s.tileId,
                name = s.name.ifBlank { "Untitled" },
                executionMode = s.executionMode,
                iconId = s.selectedIconId,
                isCustom = true,
                slotIndex = s.tileId - 1,
                timeoutMs = null,
                activeState = s.toActiveState(),
            )

            repository.createTile(tile)

            tileComponentManager.promptAddTile(
                tile.slotIndex!!,
                tile.name,
                getIconRes(tile.iconId),
            )
        }
    }

    fun deleteTile() {
        viewModelScope.launch {
            repository.deleteTile(route.tileId)
        }
    }
}
