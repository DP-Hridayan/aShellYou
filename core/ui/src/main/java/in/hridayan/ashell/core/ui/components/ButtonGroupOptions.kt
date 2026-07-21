package `in`.hridayan.ashell.core.ui.components

/**
 * Generic option for button/toggle groups.
 * Used by non-settings UI such as [CreateTileScreen].
 */
data class ButtonGroupOptions<T>(
    val value: T,
    val labelResId: Int? = null,
)
