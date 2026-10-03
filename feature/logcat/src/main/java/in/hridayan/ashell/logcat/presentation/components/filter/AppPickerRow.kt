@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.logcat.presentation.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.InstalledApp

private const val SELECTED_SHAPE_PERCENT = 50
private const val PACKAGE_NAME_ALPHA = 0.75f

/**
 * One installed app in the package picker, laid out like a contributor row. Selected apps take
 * the fully rounded shape, the primary container colour and a check mark, as profile rows do.
 */
@Composable
fun AppPickerRow(
    app: InstalledApp,
    isSelected: Boolean,
    shape: CustomCardShape,
    onToggle: (packageName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = if (isSelected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    }

    CustomCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .semantics { selected = isSelected },
        shape = if (isSelected) CustomCardShape(SELECTED_SHAPE_PERCENT) else shape,
        colors = colors,
        onClick = withHaptic(HapticFeedbackType.ToggleOn) { onToggle(app.packageName) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            AppIcon(packageName = app.packageName, size = 40.dp)

            AppLabels(app = app, modifier = Modifier.weight(1f))

            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun AppLabels(app: InstalledApp, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = app.label,
            style = MaterialTheme.typography.titleMediumEmphasized,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = app.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = PACKAGE_NAME_ALPHA),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
