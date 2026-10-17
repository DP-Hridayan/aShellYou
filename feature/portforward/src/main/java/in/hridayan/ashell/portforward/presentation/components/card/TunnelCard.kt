package `in`.hridayan.ashell.portforward.presentation.components.card

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.switch.AppSwitch
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelState
import `in`.hridayan.ashell.portforward.domain.model.TunnelStats
import `in`.hridayan.ashell.portforward.domain.model.localAddress
import `in`.hridayan.ashell.portforward.domain.model.targetAddress
import `in`.hridayan.ashell.portforward.presentation.components.status.label
import `in`.hridayan.ashell.portforward.presentation.components.status.style
import `in`.hridayan.ashell.portforward.presentation.model.TunnelCardActions
import `in`.hridayan.ashell.portforward.presentation.model.TunnelItem
import `in`.hridayan.ashell.portforward.text.resolve

/**
 * One tunnel in the list: its name, where it relays, its state and the actions that fit that state.
 *
 * [index] and [count] place the card in its section so the group's outer corners are rounded.
 */
@Composable
fun TunnelCard(
    item: TunnelItem,
    index: Int,
    count: Int,
    actions: TunnelCardActions,
    modifier: Modifier = Modifier
) {
    CustomCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = getRoundedShape(index, count),
        onClick = withHaptic { actions.onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TunnelCardHeader(item, actions.onRunningChange)
            Text(
                text = stringResource(R.string.port_forward_endpoints, item.spec.localAddress, item.spec.targetAddress),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            DeviceOfflineLine(item)
            item.runtime?.let { runtime -> TunnelStateLine(item = item, state = runtime.state, stats = runtime.stats) }
            TunnelCardButtons(item = item, state = item.runtime?.state, actions = actions)
        }
    }
}

@Composable
private fun TunnelCardHeader(item: TunnelItem, onRunningChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp)
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.ic_port_forward),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = item.spec.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val runningLabel = stringResource(if (item.isRunning) R.string.running else R.string.stopped)
        AppSwitch(
            modifier = Modifier.semantics { contentDescription = runningLabel },
            checked = item.isRunning,
            enabled = item.isRunning || item.canStart,
            onCheckedChange = onRunningChange
        )
    }
}

@Composable
private fun TunnelStateLine(item: TunnelItem, state: TunnelState, stats: TunnelStats) {
    val context = LocalContext.current
    val style = state.style()
    val label = state.label(item.spec).resolve()
    val counters = if (state == TunnelState.Active) {
        stringResource(
            R.string.port_forward_card_counters,
            stats.openConnections,
            Formatter.formatShortFileSize(context, stats.bytesReceived)
        )
    } else {
        null
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(style.icon),
            contentDescription = null,
            tint = style.color
        )
        Text(
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = style.color
        )
        (state as? TunnelState.Waiting)?.deadlineMillis?.let { WaitCountdown(it) }
        counters?.let {
            Text(
                modifier = Modifier.clearAndSetSemantics {},
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TunnelCardButtons(item: TunnelItem, state: TunnelState?, actions: TunnelCardActions) {
    val suggestedPort = ((state as? TunnelState.Failed)?.error as? TunnelError.PortInUse)?.suggestedPort
    val showOpen = item.isRunning && item.spec.looksLikeHttp && state !is TunnelState.Waiting
    val showFix = state is TunnelState.Failed
    val hasAction = showOpen || showFix || item.isRunning
    if (!hasAction && !item.hasDeviceAction(state)) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DeviceButtons(item = item, state = state, actions = actions)
        if (showOpen) {
            FilledTonalButton(onClick = withHaptic { actions.onOpen() }) { Text(stringResource(R.string.open)) }
        }
        if (item.isRunning) {
            TextButton(onClick = withHaptic { actions.onCopy() }) {
                Text(stringResource(if (item.spec.looksLikeHttp) R.string.copy_link else R.string.copy_address))
            }
        }
        suggestedPort?.let { port ->
            FilledTonalButton(onClick = withHaptic { actions.onUsePort(port) }) {
                Text(stringResource(R.string.port_forward_use_port, port))
            }
        }
        if (showFix) {
            TextButton(onClick = withHaptic { actions.onEdit() }) { Text(stringResource(R.string.edit)) }
        }
    }
}
