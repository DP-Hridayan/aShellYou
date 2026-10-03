package `in`.hridayan.ashell.logcat.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.presentation.model.LogListActions
import `in`.hridayan.ashell.logcat.presentation.model.OtherDeviceUiState

/**
 * Content for the "Other Device" tab. Logs collected before a disconnect stay visible; the
 * connect options are shown only when there is nothing to read.
 */
@Composable
fun OtherDeviceContent(
    state: OtherDeviceUiState,
    expandedIds: Set<Long>,
    listState: LazyListState,
    actions: LogListActions,
    onConnectViaWifiAdb: () -> Unit,
    onConnectViaOtg: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.list.logs.isNotEmpty() -> AutoScrollingLogList(
                logs = state.list.logs,
                expandedIds = expandedIds,
                listState = listState,
                isAutoScrolling = state.list.isAutoScrolling,
                actions = actions,
            )

            !state.isConnected -> NotConnectedPanel(
                onConnectViaWifiAdb = onConnectViaWifiAdb,
                onConnectViaOtg = onConnectViaOtg,
            )

            state.isRunning -> EmptyLogsMessage(R.string.logcat_no_logs)
            else -> EmptyLogsMessage(R.string.logcat_tap_play)
        }
    }
}

@Composable
private fun EmptyLogsMessage(@StringRes text: Int) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
