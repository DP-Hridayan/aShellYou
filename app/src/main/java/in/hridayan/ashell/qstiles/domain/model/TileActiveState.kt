package `in`.hridayan.ashell.qstiles.domain.model

/**
 * Encapsulates the "active-state" behaviour of a Quick Settings tile.
 *
 * **Toggleable tile** (`isToggleable = true`):
 *  - The tile alternates between ON and OFF on every click.
 *  - [activeCommand]   → executed when the tile is being turned ON.
 *  - [inactiveCommand] → executed when the tile is being turned OFF.
 *  - [activeTileSubtitle]   / [inactiveTileSubtitle] drive the QS panel subtitle.
 *
 * **Static tile** (`isToggleable = false`):
 *  - The tile always shows the same visual state, determined by [isActive].
 *  - Only [activeCommand] is used (it is executed on every click).
 *  - Only [activeTileSubtitle] is shown as the subtitle.
 */
data class TileActiveState(
    /** Whether the tile alternates its ON/OFF state on each click. */
    val isToggleable: Boolean = false,
    /** Current runtime state (ON = true, OFF = false). For static tiles this is the fixed state. */
    val isActive: Boolean = false,
    /** Subtitle shown in the QS panel when the tile is in the ON state. */
    val activeTileSubtitle: String = "On",
    /** Subtitle shown in the QS panel when the tile is in the OFF state. Ignored for static tiles. */
    val inactiveTileSubtitle: String = "Off",
    /** ADB command executed when turning the tile ON (or on every click for static tiles). */
    val activeCommand: String = "",
    /** ADB command executed when turning the tile OFF. Only used when [isToggleable] = true. */
    val inactiveCommand: String = "",
) {
    /** Returns the subtitle that should currently be displayed based on [isActive]. */
    val currentSubtitle: String
        get() = if (isActive) activeTileSubtitle else inactiveTileSubtitle

    /** Returns the command that should be executed for the current state transition. */
    val commandToExecute: String
        get() = if (!isToggleable || isActive) activeCommand else inactiveCommand
}