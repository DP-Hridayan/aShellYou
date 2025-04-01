package `in`.hridayan.ashell.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import `in`.hridayan.ashell.ui.theme.Shape

@Composable
fun BottomCornerRoundedCard(
    modifier: Modifier = Modifier, onClick: () -> Unit = {}, content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(Shape.cardBottomCornersRounded)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onClick() },
        shape = Shape.cardBottomCornersRounded
    ) {
        content()
    }
}