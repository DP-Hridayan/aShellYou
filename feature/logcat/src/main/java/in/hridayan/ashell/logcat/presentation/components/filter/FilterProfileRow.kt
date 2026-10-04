package `in`.hridayan.ashell.logcat.presentation.components.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
private val CHOSEN_OUTLINE_WIDTH = 2.dp
private val LEADING_ICON_SIZE = 20.dp

/**
 * One saved profile in the filter sheet. Active rows take the fully rounded shape and the primary
 * container colour, as in the commands label filter. While [isSelecting], a selection circle
 * replaces the active check, chosen rows are outlined, and every row offers Edit.
 */
@Composable
fun FilterProfileRow(
    profile: LogFilter,
    isActive: Boolean,
    isChosen: Boolean,
    isSelecting: Boolean,
    shape: CustomCardShape,
    actions: FilterProfileActions,
    modifier: Modifier = Modifier,
) {
    val colors = profileCardColors(isActive)
    val selectLabel = stringResource(if (isChosen) R.string.deselect else R.string.select)

    CustomCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .semantics { selected = if (isSelecting) isChosen else isActive },
        shape = if (isActive) CustomCardShape(ACTIVE_SHAPE_PERCENT) else shape,
        colors = colors,
        border = if (isChosen) BorderStroke(CHOSEN_OUTLINE_WIDTH, MaterialTheme.colorScheme.primary) else null,
        onClick = withHaptic { actions.onTap(profile.id) },
        onLongClick = { actions.onLongPress(profile.id) },
        onClickLabel = if (isSelecting) selectLabel else null,
        onLongClickLabel = selectLabel,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = if (isSelecting) 4.dp else 15.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LeadingIcon(isActive = isActive, isChosen = isChosen, isSelecting = isSelecting)

            ProfileDetails(profile = profile, ringColor = colors.containerColor, modifier = Modifier.weight(1f))

            if (isSelecting) {
                IconButton(onClick = withHaptic { actions.onEdit(profile.id) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = stringResource(R.string.edit),
                    )
                }
            }
        }
    }
}

@Composable
private fun profileCardColors(isActive: Boolean): CardColors = if (isActive) {
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

@Composable
private fun LeadingIcon(isActive: Boolean, isChosen: Boolean, isSelecting: Boolean) {
    val icon = when {
        isSelecting && isChosen -> R.drawable.ic_checked_filled
        isSelecting -> R.drawable.ic_checked_outline
        isActive -> R.drawable.ic_check
        else -> return
    }

    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = if (isChosen) MaterialTheme.colorScheme.primary else LocalContentColor.current,
        modifier = Modifier.size(LEADING_ICON_SIZE),
    )
}

@Composable
private fun ProfileDetails(profile: LogFilter, ringColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = profile.name.ifBlank { stringResource(R.string.untitled) },
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            FilterModeBadge(mode = profile.mode, modifier = Modifier.padding(top = 2.dp))
        }

        Text(
            text = levelSummaryText(profile.levelSummary()),
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current.copy(alpha = DESCRIPTION_ALPHA),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )

        if (profile.packages.isNotEmpty()) {
            ProfileAppIcons(
                packages = profile.packages,
                ringColor = ringColor,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
