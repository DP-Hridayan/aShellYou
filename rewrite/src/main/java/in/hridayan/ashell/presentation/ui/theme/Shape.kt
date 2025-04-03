package `in`.hridayan.ashell.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object Shape {
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