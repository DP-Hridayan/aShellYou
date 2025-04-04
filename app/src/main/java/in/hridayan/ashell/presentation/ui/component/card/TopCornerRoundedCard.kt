package `in`.hridayan.ashell.presentation.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import `in`.hridayan.ashell.presentation.ui.theme.Shape

@Composable
fun TopCornerRoundedCard(
    modifier: Modifier = Modifier, onClick: () -> Unit = {}, content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(Shape.cardTopCornersRounded)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onClick() },
        shape = Shape.cardTopCornersRounded
    ) {
        content()
    }
}