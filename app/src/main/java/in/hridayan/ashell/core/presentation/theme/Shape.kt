package `in`.hridayan.ashell.core.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Shape {
    val labelStroke = 1.dp

    val cardStrokeSmall = 1.dp
    val cardStrokeMedium = 2.dp
    val cardStrokeLarge = 4.dp

    val cardCornerSmall = RoundedCornerShape(8.dp)
    val cardCornerMedium = RoundedCornerShape(16.dp)
    val cardCornerLarge = RoundedCornerShape(25.dp)

    val cardTopCornersRounded = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 2.dp,
        bottomEnd = 2.dp
    )

    val cardBottomCornersRounded = RoundedCornerShape(
        topStart = 2.dp,
        topEnd = 2.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )
}

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
        bottomEnd = CornerSize.Percentage(percentage.toFloat())
    )

    constructor(all: Dp) : this(
        topStart = CornerSize.Absolute(all),
        topEnd = CornerSize.Absolute(all),
        bottomStart = CornerSize.Absolute(all),
        bottomEnd = CornerSize.Absolute(all)
    )

    constructor(top: Dp, bottom: Dp) : this(
        topStart = CornerSize.Absolute(top),
        topEnd = CornerSize.Absolute(top),
        bottomStart = CornerSize.Absolute(bottom),
        bottomEnd = CornerSize.Absolute(bottom)
    )

    constructor(topStart: Dp, topEnd: Dp, bottomStart: Dp, bottomEnd: Dp) : this(
        topStart = CornerSize.Absolute(topStart),
        topEnd = CornerSize.Absolute(topEnd),
        bottomStart = CornerSize.Absolute(bottomStart),
        bottomEnd = CornerSize.Absolute(bottomEnd)
    )
}

fun CornerSize.toDp(heightPx: Float, density: Density): Dp {
    return when (this) {
        is CornerSize.Absolute -> dp
        is CornerSize.Percentage -> {
            with(density) {
                (heightPx * (percent / 100f)).toDp()
            }
        }
    }
}

object CardCornerShape {
    val SINGLE_CARD = CustomCardShape(24.dp)

    val FIRST_CARD = CustomCardShape(top = 24.dp, bottom = 4.dp)

    val MIDDLE_CARD = CustomCardShape(all = 4.dp)

    val LAST_CARD = CustomCardShape(top = 4.dp, bottom = 24.dp)

    fun getRoundedShape(
        index: Int,
        size: Int,
    ): CustomCardShape {
        return when {
            size == 1 -> SINGLE_CARD

            index == 0 -> FIRST_CARD

            index == size - 1 -> LAST_CARD

            else -> MIDDLE_CARD
        }
    }
}

