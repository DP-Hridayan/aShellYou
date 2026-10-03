package `in`.hridayan.ashell.logcat.presentation.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.presentation.model.FilterProfileActions
import `in`.hridayan.ashell.logcat.presentation.model.levelSummary

private const val ACTIVE_SHAPE_PERCENT = 50
private const val DESCRIPTION_ALPHA = 0.75f

/**
 * One saved profile in the filter sheet. Tapping the row turns the profile on or off; active rows
 * take the fully rounded shape and the primary container colour, as in the commands label filter.
 */
@Composable
fun FilterProfileRow(
    profile: LogFilter,
    isActive: Boolean,
    shape: CustomCardShape,
    actions: FilterProfileActions,
    modifier: Modifier = Modifier,
) {
    val colors = if (isActive) {
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
            .semantics { selected = isActive },
        shape = if (isActive) CustomCardShape(ACTIVE_SHAPE_PERCENT) else shape,
        colors = colors,
        onClick = withHaptic { actions.onToggle(profile.id) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (isActive) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            ProfileLabels(profile = profile, modifier = Modifier.weight(1f))

            ProfileButtons(profile = profile, actions = actions)
        }
    }
}

@Composable
private fun ProfileButtons(profile: LogFilter, actions: FilterProfileActions) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        IconButton(onClick = withHaptic { actions.onEdit(profile.id) }) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = stringResource(R.string.edit),
            )
        }

        IconButton(onClick = withHaptic { actions.onDelete(profile) }) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = stringResource(R.string.delete),
            )
        }
    }
}

@Composable
private fun ProfileLabels(profile: LogFilter, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = profile.name.ifBlank { stringResource(R.string.untitled) },
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = levelSummaryText(profile.levelSummary()),
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = DESCRIPTION_ALPHA),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
