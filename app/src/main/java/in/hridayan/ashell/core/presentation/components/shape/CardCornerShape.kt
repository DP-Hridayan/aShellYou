package `in`.hridayan.ashell.core.presentation.components.shape

import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape

object CardCornerShape {
    val SINGLE_CARD = CustomCardShape(16.dp)

    val FIRST_CARD = CustomCardShape(top = 16.dp, bottom = 4.dp)

    val MIDDLE_CARD = CustomCardShape(all = 4.dp)

    val LAST_CARD = CustomCardShape(top = 4.dp, bottom = 16.dp)

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
