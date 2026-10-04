package `in`.hridayan.ashell.core.presentation.components.radio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape

private const val SELECTED_CORNER_PERCENT = 50
private const val DESCRIPTION_ALPHA = 0.75f

/**
 * One option in a vertical group of single-choice cards.
 *
 * [index] and [count] place the card in its group, so the outer corners of the group are rounded and
 * the inner ones are tight. The selected card is fully rounded instead. An optional [description]
 * is shown under the label.
 */
@Composable
fun RadioOptionCard(
    label: String,
    selected: Boolean,
    index: Int,
    count: Int,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    val select = withHaptic(HapticFeedbackType.ToggleOn) { onSelect() }

    CustomCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = if (selected) CustomCardShape(SELECTED_CORNER_PERCENT) else getRoundedShape(index, count),
        colors = radioCardColors(selected),
        onClick = select
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = DESCRIPTION_ALPHA)
                    )
                }
            }

            RadioButton(
                selected = selected,
                onClick = select,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun radioCardColors(selected: Boolean): CardColors =
    if (selected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
