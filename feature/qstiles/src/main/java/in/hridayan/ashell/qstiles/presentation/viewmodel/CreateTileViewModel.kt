package `in`.hridayan.ashell.qstiles.presentation.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.qstiles.data.model.MaterialIconEntry
import `in`.hridayan.ashell.qstiles.data.provider.TileComponentManager
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider.migrateIconId
import `in`.hridayan.ashell.qstiles.domain.model.FontLoadState
import `in`.hridayan.ashell.qstiles.domain.model.MaterialIconStyle
import `in`.hridayan.ashell.qstiles.domain.model.TileActiveState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.processor.TileCommandKeywordProcessor
import `in`.hridayan.ashell.qstiles.domain.repository.MaterialIconRepository
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import `in`.hridayan.ashell.qstiles.presentation.model.CreateNewTileScreenUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val DEFAULT_ACTIVE_SUBTITLE = "On"
private const val DEFAULT_INACTIVE_SUBTITLE = "Off"
private const val DEFAULT_TILE_NAME = "Untitled"
private const val NAME_VALIDATION_DEBOUNCE_MS = 250L

@HiltViewModel
class CreateTileViewModel @Inject constructor(
    private val repository: TileRepository,
    private val keywordProcessor: TileCommandKeywordProcessor,
    private val tileComponentManager: TileComponentManager,
    private val materialIconRepository: MaterialIconRepository,
    @param:ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<NavRoutes.CreateTileScreen>()

    private val _state = MutableStateFlow(CreateNewTileScreenUiState(tileId = route.tileId))
    val state: StateFlow<CreateNewTileScreenUiState> = _state.asStateFlow()

    val nameState = TextFieldState()
    val activeCommandState = TextFieldState()
    val inactiveCommandState = TextFieldState()
    val activeSubtitleState = TextFieldState(DEFAULT_ACTIVE_SUBTITLE)
    val inactiveSubtitleState = TextFieldState(DEFAULT_INACTIVE_SUBTITLE)

    private val tilesFlow = repository.getTiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            materialIconRepository.fontState.collect { fontState ->
                _state.update {
                    it.copy(
                        fontLoadState = fontState,
                        iconResults = if (fontState is FontLoadState.Ready) fontState.icons else emptyList()
                    )
                }
                updateSuggestions()
            }
        }
        observeTextFields()
        loadExistingTile()
    }

    private fun observeTextFields() {
        viewModelScope.launch {
            snapshotFlow { nameState.text.toString() }
                .drop(1)
                .distinctUntilChanged()
                .collectLatest { name ->
                    _state.update { it.copy(nameError = null) }
                    updateSuggestions()
                    validateDuplicateName(name)
                }
        }
        viewModelScope.launch {
            snapshotFlow { activeCommandState.text.toString() }
                .drop(1)
                .distinctUntilChanged()
                .collect { updateSuggestions() }
        }
    }

    private suspend fun validateDuplicateName(name: String) {
        delay(NAME_VALIDATION_DEBOUNCE_MS.milliseconds)
        val tiles = tilesFlow.first { it.isNotEmpty() }
        val isDuplicate = tiles.any {
            it.name.equals(name, ignoreCase = true) && it.id != route.tileId
        }
        _state.update {
            it.copy(
                nameError = if (isDuplicate) {
                    context.getString(R.string.duplicate_tile_name_error_msg)
                } else {
                    null
                }
            )
        }
    }

    private fun loadExistingTile() {
        viewModelScope.launch {
            repository.getTileOnce(route.tileId)?.let { config ->
                nameState.setTextAndPlaceCursorAtEnd(config.name)
                activeCommandState.setTextAndPlaceCursorAtEnd(config.activeState.activeCommand.text)
                inactiveCommandState.setTextAndPlaceCursorAtEnd(config.activeState.inactiveCommand.text)
                activeSubtitleState.setTextAndPlaceCursorAtEnd(config.activeState.activeTileSubtitle.text)
                inactiveSubtitleState.setTextAndPlaceCursorAtEnd(config.activeState.inactiveTileSubtitle.text)
                _state.update {
                    it.copy(
                        executionMode = config.executionMode,
                        selectedIconId = migrateIconId(config.iconId),
                        selectedIconStyle = config.iconStyle,
                        isUpdateMode = true,
                        isToggleable = config.activeState.isToggleable,
                        isActive = config.activeState.isActive,
                        collapsePanelOnClick = config.collapsePanelOnClick,
                    )
                }
                updateSuggestions()
                loadIcons()
            }
        }
    }

    fun onExecutionModeChange(mode: Int) =
        _state.update { it.copy(executionMode = mode) }

    fun onIconStyleSelected(style: MaterialIconStyle) {
        _state.update { it.copy(selectedIconStyle = style) }
        viewModelScope.launch {
            materialIconRepository.loadFont(style)
        }
    }

    fun onIconQueryChange(query: TextFieldValue) {
        _state.update { it.copy(iconSearchQuery = query) }
        filterMaterialIcons(query.text)
    }

    fun loadIcons() {
        viewModelScope.launch {
            materialIconRepository.loadFont(_state.value.selectedIconStyle)
        }
    }

    fun onIconSelected(entry: MaterialIconEntry) {
        _state.update { it.copy(selectedIconId = entry.name) }
        viewModelScope.launch {
            materialIconRepository.renderAndCacheIcon(
                _state.value.selectedIconStyle,
                entry.name,
                entry.codepoint
            )
        }
    }

    fun onSuggestedIconSelected(iconId: String) {
        _state.update { it.copy(selectedIconId = iconId) }
        viewModelScope.launch {
            val fontState = _state.value.fontLoadState
            if (fontState is FontLoadState.Ready) {
                val entry = fontState.icons.find { it.name == iconId }
                if (entry != null) {
                    materialIconRepository.renderAndCacheIcon(
                        _state.value.selectedIconStyle,
                        entry.name,
                        entry.codepoint
                    )
                }
            }
        }
    }

    private fun filterMaterialIcons(query: String) {
        val fontState = _state.value.fontLoadState
        if (fontState !is FontLoadState.Ready) return
        if (query.isBlank()) {
            _state.update { it.copy(iconResults = fontState.icons) }
            return
        }
        _state.update {
            it.copy(
                iconResults = fontState.icons.filter { icon ->
                    icon.name.contains(query, ignoreCase = true)
                }
            )
        }
    }

    fun onToggleableChange(isToggleable: Boolean) =
        _state.update { it.copy(isToggleable = isToggleable) }

    fun onActiveStateChange(isActive: Boolean) =
        _state.update { it.copy(isActive = isActive) }

    fun onCollapsePanelOnClickChange(enabled: Boolean) =
        _state.update { it.copy(collapsePanelOnClick = enabled) }

    private fun updateSuggestions() {
        val s = _state.value
        val textToAnalyze = "${nameState.text} ${activeCommandState.text}"
        val keywords = keywordProcessor.extractKeywords(textToAnalyze)

        val scored = mutableMapOf<String, Int>()

        if (s.fontLoadState is FontLoadState.Ready) {
            val materialIcons = s.fontLoadState.icons
            materialIcons.forEach { materialIcon ->
                keywords.forEach { keyword ->
                    if (materialIcon.name.contains(keyword, ignoreCase = true)) {
                        scored[materialIcon.name] = (scored[materialIcon.name] ?: 0) + 1
                    }
                }
            }
        }

        val topIcons = scored.toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }

        _state.update { it.copy(suggestedIcons = topIcons) }
    }

    private fun CreateNewTileScreenUiState.toActiveState(): TileActiveState {
        val activeCmd = TextFieldValue(activeCommandState.text.toString())
        val inactiveCmd = if (isToggleable) {
            TextFieldValue(inactiveCommandState.text.toString())
        } else {
            TextFieldValue("")
        }
        val activeSub = TextFieldValue(activeSubtitleState.text.toString())
        val inactiveSub = if (isToggleable) {
            TextFieldValue(inactiveSubtitleState.text.toString())
        } else {
            activeSub
        }
        return TileActiveState(
            isToggleable = isToggleable,
            isActive = isActive,
            activeCommand = activeCmd,
            inactiveCommand = inactiveCmd,
            activeTileSubtitle = activeSub,
            inactiveTileSubtitle = inactiveSub,
        )
    }

    fun createTile() {
        viewModelScope.launch {
            val s = _state.value

            val tile = TileConfig(
                id = s.tileId,
                name = nameState.text.toString().ifBlank { DEFAULT_TILE_NAME },
                executionMode = s.executionMode,
                iconId = s.selectedIconId,
                iconStyle = s.selectedIconStyle,
                isCustom = true,
                slotIndex = s.tileId - 1,
                timeoutMs = null,
                collapsePanelOnClick = s.collapsePanelOnClick,
                activeState = s.toActiveState(),
            )

            repository.createTile(tile)

            val tileIcon = buildTileIcon(tile.iconId, tile.iconStyle)

            tileComponentManager.promptAddTile(
                tile.slotIndex!!,
                tile.name,
                tileIcon,
            )
        }
    }

    private suspend fun buildTileIcon(iconId: String, iconStyle: MaterialIconStyle): Icon {
        val bitmapFile = materialIconRepository.getCachedIconBitmap(iconStyle, iconId)

        if (bitmapFile?.exists() == true) {
            val bitmap = BitmapFactory.decodeFile(bitmapFile.absolutePath)
            return Icon.createWithBitmap(bitmap)
        }

        val fontState = materialIconRepository.loadFont(iconStyle)
        val entry = fontState.icons.find { it.name == iconId }

        if (entry != null) {
            val newFile =
                materialIconRepository.renderAndCacheIcon(iconStyle, entry.name, entry.codepoint)
                    .getOrNull()
            if (newFile?.exists() == true) {
                val bitmap = BitmapFactory.decodeFile(newFile.absolutePath)
                return Icon.createWithBitmap(bitmap)
            }
        }

        return Icon.createWithResource(context, R.drawable.ic_adb)
    }

    fun deleteTile() {
        viewModelScope.launch {
            repository.deleteTile(route.tileId)
        }
    }
}
