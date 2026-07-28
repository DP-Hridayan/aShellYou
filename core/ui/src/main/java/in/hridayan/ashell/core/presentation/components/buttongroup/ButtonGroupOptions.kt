package `in`.hridayan.ashell.core.presentation.components.buttongroup

/**
 * Generic option for button/toggle groups.
 * Used by non-settings UI such as [CreateTileScreen].
 */
data class ButtonGroupOptions<T>(
    val value: T,
    val labelResId: Int? = null,
)