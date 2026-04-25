package `in`.hridayan.ashell.core.presentation.components.card

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape

@Composable
fun TopCornerRoundedCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    content: @Composable () -> Unit
) {
    CustomCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = CardCornerShape.FIRST_CARD,
        onClick = onClick
    ) {
        content()
    }
}