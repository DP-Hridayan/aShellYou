@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.commandexamples.presentation.component.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.ui.theme.Shape

@Composable
fun LabelChip(
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    showCrossIcon: Boolean = false,
    commandViewModel: CommandViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current

    Row(
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
                horizontal = 2.dp,
                vertical = 2.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
        if (showCrossIcon) {
            FilledIconButton(
                onClick = {
                    weakHaptic()
                    commandViewModel.onLabelRemove(label)
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
