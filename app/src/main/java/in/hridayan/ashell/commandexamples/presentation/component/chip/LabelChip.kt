package `in`.hridayan.ashell.commandexamples.presentation.component.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens
import `in`.hridayan.ashell.core.presentation.ui.theme.Shape

@Composable
fun LabelChip(
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .clip(Shape.cardCornerMedium)
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.primaryContainer
            )
            .border(
                width = Shape.labelStroke,
                shape = Shape.cardCornerMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            .padding(
                horizontal = Dimens.paddingSmall,
                vertical = Dimens.paddingExtraSmall
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
