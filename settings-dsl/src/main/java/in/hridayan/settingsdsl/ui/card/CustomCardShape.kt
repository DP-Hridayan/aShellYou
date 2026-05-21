package `in`.hridayan.settingsdsl.ui.card

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Describes the corner radius of each corner of a settings card.
 *
 * Supports both absolute [Dp] values and percentage-of-height values.
 * Used internally by [CustomCard] to animate corner shapes on press/hover.
 */
sealed class CornerSize {
    data class Absolute(val dp: Dp) : CornerSize()
    data class Percentage(val percent: Float) : CornerSize()
}

internal fun CornerSize.toDp(heightPx: Float, density: Density): Dp =
    when (this) {
        is CornerSize.Absolute -> dp
        is CornerSize.Percentage -> with(density) { (heightPx * (percent / 100f)).toDp() }
    }

/**
 * A shape descriptor for settings list cards.
 *
 * Each corner can be independently sized. Auto-computed by the resolver based on
 * an item's position within its visibility-filtered group.
 *
 * You should not need to construct this directly — the library creates it for you.
 */
data class CustomCardShape(
    val topStart: CornerSize = CornerSize.Absolute(0.dp),
    val topEnd: CornerSize = CornerSize.Absolute(0.dp),
    val bottomStart: CornerSize = CornerSize.Absolute(0.dp),
    val bottomEnd: CornerSize = CornerSize.Absolute(0.dp),
) {
    constructor(percentage: Int) : this(
        topStart = CornerSize.Percentage(percentage.toFloat()),
        topEnd = CornerSize.Percentage(percentage.toFloat()),
        bottomStart = CornerSize.Percentage(percentage.toFloat()),
        bottomEnd = CornerSize.Percentage(percentage.toFloat()),
    )

    constructor(all: Dp) : this(
        topStart = CornerSize.Absolute(all),
        topEnd = CornerSize.Absolute(all),
        bottomStart = CornerSize.Absolute(all),
        bottomEnd = CornerSize.Absolute(all),
    )

    constructor(top: Dp, bottom: Dp) : this(
        topStart = CornerSize.Absolute(top),
        topEnd = CornerSize.Absolute(top),
        bottomStart = CornerSize.Absolute(bottom),
        bottomEnd = CornerSize.Absolute(bottom),
    )

    constructor(topStart: Dp, topEnd: Dp, bottomStart: Dp, bottomEnd: Dp) : this(
        topStart = CornerSize.Absolute(topStart),
        topEnd = CornerSize.Absolute(topEnd),
        bottomStart = CornerSize.Absolute(bottomStart),
        bottomEnd = CornerSize.Absolute(bottomEnd),
    )
}

/** Auto-computes [CustomCardShape] for a given [index] within a list of [size] items. */
internal fun cardShapeForPosition(index: Int, size: Int): CustomCardShape = when {
    size == 1 -> CustomCardShape(all = 24.dp)
    index == 0 -> CustomCardShape(top = 24.dp, bottom = 4.dp)
    index == size - 1 -> CustomCardShape(top = 4.dp, bottom = 24.dp)
    else -> CustomCardShape(all = 4.dp)
}
