package `in`.hridayan.ashell.core.presentation.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * A pill-shaped card composable that can be optionally clickable.
 *
 * This composable provides customizable colors, elevation, borders, and supports
 * click actions if enabled. Use it when you want a smooth, rounded card with
 * flexible content inside.
 *
 * @param modifier [Modifier] to be applied to the card.
 *  Do not add [Modifier.clickable], use the parameter [clickable] boolean to enable or disable clickable and use the parameter [onClick] for defining the click action
 * @param colors Defines the background and content colors of the card.
 * @param elevation Controls the shadow depth around the card.
 * @param border Optional border stroke to draw around the card.
 * @param clickable Whether the card is clickable.
 * @param onClick Action to perform when the card is clicked.
 * @param content The composable content to be displayed inside the card.
 */
@Composable
fun PillShapedCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    clickable: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable (ColumnScope.() -> Unit)
) {
    var cardHeight by remember { mutableStateOf(0.dp) }
    val screenDensity = LocalDensity.current
    val pillCornerShape = RoundedCornerShape(cardHeight / 2)

    Card(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                cardHeight = with(screenDensity) { coordinates.size.height.toDp() }
            }
            .clip(pillCornerShape)
            .clickable(enabled = clickable, onClick = onClick),
        shape = pillCornerShape,
        colors = colors,
        elevation = elevation,
        border = border
    ) {
        content()
    }
}