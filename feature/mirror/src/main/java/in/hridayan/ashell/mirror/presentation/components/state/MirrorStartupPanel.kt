package `in`.hridayan.ashell.mirror.presentation.components.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.mirror.domain.model.StartStep

private val StepIconSize = 24.dp
private val PanelMaxWidth = 360.dp

/** Shown over the black stage until the first video arrives, so the wait never looks frozen. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MirrorStartupPanel(
    step: StartStep?,
    serverAlreadyDeployed: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .widthIn(max = PanelMaxWidth)
                .padding(24.dp)
        ) {
            ContainedLoadingIndicator()

            Column {
                StartStep.entries.forEach { candidate ->
                    StepRow(
                        label = stringResource(candidate.labelRes(serverAlreadyDeployed)),
                        progress = progressOf(candidate, current = step)
                    )
                }
            }
        }
    }
}

private enum class StepProgress { DONE, ACTIVE, PENDING }

/** A null [current] means the session is past every start step and waiting for the first frame. */
private fun progressOf(step: StartStep, current: StartStep?): StepProgress = when {
    current == null || step.ordinal < current.ordinal -> StepProgress.DONE
    step == current -> StepProgress.ACTIVE
    else -> StepProgress.PENDING
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StepRow(label: String, progress: StepProgress) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            when (progress) {
                StepProgress.DONE -> Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(StepIconSize)
                )

                StepProgress.ACTIVE -> LoadingIndicator(modifier = Modifier.size(StepIconSize))

                StepProgress.PENDING -> Icon(
                    imageVector = Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(StepIconSize)
                )
            }
        }
    ) {
        Text(
            text = label,
            color = if (progress == StepProgress.PENDING) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

private fun StartStep.labelRes(serverAlreadyDeployed: Boolean): Int = when (this) {
    StartStep.PREPARING ->
        if (serverAlreadyDeployed) R.string.mirror_step_preparing_skipped else R.string.mirror_step_preparing

    StartStep.STARTING_SERVER -> R.string.mirror_step_starting
    StartStep.CONNECTING -> R.string.mirror_step_connecting
}
