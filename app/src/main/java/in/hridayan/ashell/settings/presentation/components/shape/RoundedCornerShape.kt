package `in`.hridayan.ashell.settings.presentation.components.shape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun getRoundedShape(
    index: Int,
    size: Int,
    radius: Dp = 16.dp,
    middleRadius: Dp = 4.dp
): RoundedCornerShape {
    return when {
        size == 1 -> RoundedCornerShape(radius)
        index == 0 -> RoundedCornerShape(
            topStart = radius,
            topEnd = radius,
            bottomStart = middleRadius,
            bottomEnd = middleRadius
        )

        index == size - 1 -> RoundedCornerShape(
            topStart = middleRadius,
            topEnd = middleRadius,
            bottomStart = radius,
            bottomEnd = radius
        )

        else -> RoundedCornerShape(middleRadius)
    }
}
