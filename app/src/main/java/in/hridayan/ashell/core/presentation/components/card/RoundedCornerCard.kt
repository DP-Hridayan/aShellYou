package `in`.hridayan.ashell.core.presentation.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun RoundedCornerCard(
    modifier: Modifier = Modifier,
    roundedCornerShape: RoundedCornerShape,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    paddingValues: PaddingValues = PaddingValues(vertical = 1.dp, horizontal = 15.dp),
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .padding(paddingValues)
            .clip(roundedCornerShape)
            .clickable(enabled = true, onClick = onClick),
        shape = roundedCornerShape,
        elevation = elevation,
        border = border,
        colors = colors
    ) {
        content()
    }
}
