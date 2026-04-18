package `in`.hridayan.ashell.qstiles.presentation.viewmodel

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.R
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTileViewModel @Inject constructor(
    private val repository: TileRepository,
    private val keywordProcessor: TileCommandKeywordProcessor,
    private val tileComponentManager: TileComponentManager,
    @param:ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<NavRoutes.CreateTileScreen>()

    private val _state = MutableStateFlow(CreateNewTileScreenUiState(tileId = route.tileId))
    val state: StateFlow<CreateNewTileScreenUiState> = _state.asStateFlow()

    private val _iconsList = MutableStateFlow(TileIconProvider.icons)
    val iconsList: StateFlow<List<TileIcon>> = _iconsList.asStateFlow()

    private val tilesFlow = repository.getTiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var nameValidationJob: Job? = null

    init {
        loadExistingTile()
    }

    private fun loadExistingTile() {
        viewModelScope.launch {
            repository.getTileOnce(route.tileId)?.let { config ->
                _state.update {
                    it.copy(
                        nameField = TextFieldValue(text = config.name),
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
                suggestIcons(config.activeState.activeCommand.text)
            }
        }
    }

    fun onNameChange(fieldValue: TextFieldValue) {
        _state.update {
            it.copy(
                nameField = fieldValue,
                nameError = null
            )
        }

        nameValidationJob?.cancel()

        nameValidationJob = viewModelScope.launch {
            delay(250)

            val tiles = tilesFlow.first { it.isNotEmpty() }

            val isDuplicate = tiles.any {
                it.name.equals(fieldValue.text, ignoreCase = true) &&
                        it.id != route.tileId
            }

            _state.update {
                it.copy(
                    nameError = if (isDuplicate)
                        context.getString(R.string.duplicate_tile_name_error_msg)
                    else null
                )
            }
        }
    }

    fun onActiveCommandChange(commandField: TextFieldValue) {
        _state.update { it.copy(activeCommand = commandField) }
        suggestIcons(commandField.text)
    }

    fun onInactiveCommandChange(command: TextFieldValue) =
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

    fun onActiveSubtitleChange(subtitle: TextFieldValue) =
        _state.update { it.copy(activeSubtitle = subtitle) }

    fun onInactiveSubtitleChange(subtitle: TextFieldValue) =
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
        inactiveCommand = if (isToggleable) inactiveCommand else TextFieldValue(""),
        activeTileSubtitle = activeSubtitle,
        inactiveTileSubtitle = if (isToggleable) inactiveSubtitle else activeSubtitle,
    )

    fun createTile() {
        viewModelScope.launch {
            val s = _state.value

            val tile = TileConfig(
                id = s.tileId,
                name = s.nameField.text.ifBlank { "Untitled" },
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
