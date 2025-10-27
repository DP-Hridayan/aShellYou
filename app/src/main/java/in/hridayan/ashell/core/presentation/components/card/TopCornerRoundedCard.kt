package `in`.hridayan.ashell.core.presentation.components.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import `in`.hridayan.ashell.core.presentation.theme.Shape

@Composable
fun TopCornerRoundedCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(Shape.cardTopCornersRounded)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = Shape.cardTopCornersRounded
    ) {
        content()
    }
}