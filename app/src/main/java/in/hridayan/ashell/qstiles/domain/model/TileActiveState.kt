package `in`.hridayan.ashell.qstiles.domain.model

import androidx.compose.ui.text.input.TextFieldValue
import `in`.hridayan.ashell.core.common.serializers.TextFieldValueSerializer
import kotlinx.serialization.Serializable

/**
 * Encapsulates the "active-state" behaviour of a Quick Settings tile.
 *
 * **Toggleable tile** (`isToggleable = true`):
 *  - The tile alternates between ON and OFF on every click.
 *  - [activeCommand]   → executed when the tile is ALREADY ON (to turn it OFF).
 *  - [inactiveCommand] → executed when the tile is ALREADY OFF (to turn it ON).
 *  - [activeTileSubtitle]   / [inactiveTileSubtitle] drive the QS panel subtitle.
 *
 * **Static tile** (`isToggleable = false`):
 *  - The tile always shows the same visual state, determined by [isActive].
 *  - Only [activeCommand] is used (it is executed on every click).
 *  - Only [activeTileSubtitle] is shown as the subtitle.
 */
@Serializable
data class TileActiveState(
    /** Whether the tile alternates its ON/OFF state on each click. */
    val isToggleable: Boolean = false,
    /** Current runtime state (ON = true, OFF = false). For static tiles this is the fixed state. */
    val isActive: Boolean = false,
    /** Subtitle shown in the QS panel when the tile is in the ON state. */
    @Serializable(with = TextFieldValueSerializer::class)
    val activeTileSubtitle: TextFieldValue = TextFieldValue("On"),
    /** Subtitle shown in the QS panel when the tile is in the OFF state. Ignored for static tiles. */
    @Serializable(with = TextFieldValueSerializer::class)
    val inactiveTileSubtitle: TextFieldValue = TextFieldValue("Off"),
    /** ADB command executed when the tile is ALREADY ON (to turn it OFF). */
    @Serializable(with = TextFieldValueSerializer::class)
    val activeCommand: TextFieldValue = TextFieldValue(""),
    /** ADB command executed when the tile is ALREADY OFF (to turn it ON). Only used when [isToggleable] = true. */
    @Serializable(with = TextFieldValueSerializer::class)
    val inactiveCommand: TextFieldValue = TextFieldValue(""),
) {
    /** Returns the subtitle that should currently be displayed based on [isActive]. */
    val currentSubtitle: String
        get() = if (isActive) activeTileSubtitle.text else inactiveTileSubtitle.text

    /** Returns the command that should be executed for the current state transition. */
    val commandToExecute: String
        get() = if (!isToggleable || isActive) activeCommand.text else inactiveCommand.text
}