package `in`.hridayan.ashell.qstiles.presentation.model

import androidx.compose.ui.text.input.TextFieldValue
import `in`.hridayan.ashell.qstiles.domain.model.TileActiveState
import `in`.hridayan.ashell.qstiles.domain.model.TileExecutionMode

/**
 * UI state for the Create / Edit Tile screen.
 *
 * The fields map 1-to-1 with [TileActiveState] so the ViewModel can build
 * the data class without any extra logic in the screen composable.
 */
data class CreateNewTileScreenUiState(
    val tileId: Int = 0,
    val name: String = "",
    val executionMode: Int = TileExecutionMode.SHIZUKU,
    val selectedIconId: String = "terminal",
    val isUpdateMode: Boolean = false,

    val suggestedIcons: List<String> = emptyList(),
    val iconSearchQuery: TextFieldValue = TextFieldValue(""),

    /** Whether the tile alternates its state on each click. */
    val isToggleable: Boolean = false,
    /** Static / initial active state – for static tiles this is fixed; for toggleable it's the starting state. */
    val isActive: Boolean = false,
    /** Command executed when turning the tile ON (or on every click for static tiles). */
    val activeCommand: String = "",
    /** Command executed when turning the tile OFF. Shown only when [isToggleable] = true. */
    val inactiveCommand: String = "",
    /** Subtitle shown when the tile is ON. */
    val activeSubtitle: String = "On",
    /** Subtitle shown when the tile is OFF. Shown only when [isToggleable] = true. */
    val inactiveSubtitle: String = "Off",
)