package `in`.hridayan.ashell.settings.presentation.components.card

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RoundedCornerCard(
    modifier: Modifier = Modifier,
    roundedShape: RoundedCornerShape,
    containerColor : Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    paddingValues: PaddingValues = PaddingValues(vertical = 1.dp, horizontal = 15.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .padding(paddingValues)
            .clip(roundedShape),
        shape = roundedShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        content()
    }
}
