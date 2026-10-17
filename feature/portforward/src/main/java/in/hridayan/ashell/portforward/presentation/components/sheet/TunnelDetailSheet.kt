@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.portforward.presentation.components.sheet

import android.content.Context
import android.text.format.DateFormat
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.domain.model.TunnelEvent
import `in`.hridayan.ashell.portforward.domain.model.TunnelRuntime
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.localAddress
import `in`.hridayan.ashell.portforward.domain.model.targetAddress
import `in`.hridayan.ashell.portforward.presentation.components.status.describe
import `in`.hridayan.ashell.portforward.presentation.components.status.label
import `in`.hridayan.ashell.portforward.presentation.model.TunnelDetailActions
import `in`.hridayan.ashell.portforward.presentation.model.TunnelItem
import `in`.hridayan.ashell.portforward.text.resolve

/** Everything about one tunnel: where it relays, its counters and its recent activity. */
@Composable
fun TunnelDetailSheet(item: TunnelItem, actions: TunnelDetailActions) {
    ModalBottomSheet(
        onDismissRequest = actions.onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header") { DetailHeader(item) }
            item(key = "buttons") { DetailButtons(item, actions) }
            item.runtime?.let { runtime ->
                item(key = "stats") { DetailStats(runtime) }
                item(key = "activity_title") { ActivityTitle(item.spec, runtime, actions.onCopyActivity) }
                items(runtime.events) { event -> ActivityRow(item.spec, event) }
            }
            item(key = "bottom") { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun DetailHeader(item: TunnelItem) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = item.spec.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            text = stringResource(R.string.port_forward_endpoints, item.spec.localAddress, item.spec.targetAddress),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        item.runtime?.let {
            Text(
                text = it.state.label(item.spec).resolve(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DetailButtons(item: TunnelItem, actions: TunnelDetailActions) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick = withHaptic { actions.onRunningChange(!item.isRunning) }) {
            Text(stringResource(if (item.isRunning) R.string.stop else R.string.start))
        }
        OutlinedButton(onClick = withHaptic { actions.onEdit() }) { Text(stringResource(R.string.edit)) }
        if (item.isRunning && item.spec.looksLikeHttp) {
            OutlinedButton(onClick = withHaptic { actions.onOpen() }) { Text(stringResource(R.string.open)) }
        }
        TextButton(onClick = withHaptic { actions.onDelete() }) { Text(stringResource(R.string.delete)) }
    }
}

@Composable
private fun DetailStats(runtime: TunnelRuntime) {
    val context = LocalContext.current
    val stats = runtime.stats
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HorizontalDivider()
        Text(
            text = stringResource(
                R.string.port_forward_detail_connections,
                stats.openConnections,
                stats.totalConnections,
                stats.failedConnections
            ),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(
                R.string.port_forward_detail_traffic,
                Formatter.formatShortFileSize(context, stats.bytesReceived),
                Formatter.formatShortFileSize(context, stats.bytesSent)
            ),
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider()
    }
}

@Composable
private fun ActivityTitle(spec: TunnelSpec, runtime: TunnelRuntime, onCopy: (String) -> Unit) {
    val context = LocalContext.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = stringResource(R.string.activity),
            style = MaterialTheme.typography.titleSmall
        )
        if (runtime.events.isNotEmpty()) {
            TextButton(onClick = withHaptic { onCopy(activityText(context, spec, runtime.events)) }) {
                Text(stringResource(R.string.copy))
            }
        }
    }
}

@Composable
private fun ActivityRow(spec: TunnelSpec, event: TunnelEvent) {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = DateFormat.getTimeFormat(context).format(event.atMillis),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = event.kind.describe(context, spec), style = MaterialTheme.typography.bodySmall)
    }
}

private fun activityText(context: Context, spec: TunnelSpec, events: List<TunnelEvent>): String {
    val timeFormat = DateFormat.getTimeFormat(context)
    return events.joinToString(
        LINE_SEPARATOR
    ) { "${timeFormat.format(it.atMillis)} ${it.kind.describe(context, spec)}" }
}

private const val LINE_SEPARATOR = "\n"
