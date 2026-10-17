@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.portforward.presentation.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.core.common.domain.model.PortForwardDefaults
import `in`.hridayan.ashell.core.common.network.LocalNetworkPermission
import `in`.hridayan.ashell.core.common.notification.NotificationChannelManager
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.presentation.components.dialog.ChoiceDialog
import `in`.hridayan.ashell.portforward.presentation.components.dialog.ConfirmDialog
import `in`.hridayan.ashell.portforward.presentation.permission.rememberLocalNetworkAccess
import `in`.hridayan.ashell.portforward.presentation.viewmodel.PortForwardSettingsDialog
import `in`.hridayan.ashell.portforward.presentation.viewmodel.PortForwardSettingsViewModel
import `in`.hridayan.settingsgraph.ui.SettingsColumn

/** The settings that apply to every tunnel. */
@Composable
fun PortForwardSettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: PortForwardSettingsViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val settings = LocalSettings.current
    val dialog by viewModel.dialog.collectAsStateWithLifecycle()
    val localNetwork = rememberLocalNetworkAccess()
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()

    val hapticsEnabled = settings[SettingsKeys.HapticsAndVibration]
    val lanSharingAllowed = settings[SettingsKeys.PortForwardAllowLanSharing]
    val connectTimeout = PortForwardDefaults.sanitizeConnectTimeout(
        settings[SettingsKeys.PortForwardConnectTimeoutSeconds]
    )
    val maxConnections = PortForwardDefaults.sanitizeMaxConnections(settings[SettingsKeys.PortForwardMaxConnections])
    val waitTimeout = PortForwardDefaults.sanitizeWaitTimeout(settings[SettingsKeys.PortForwardWaitTimeoutMinutes])
    val resumeOnReconnect = settings[SettingsKeys.PortForwardResumeOnReconnect]
    val localNetworkStatus = stringResource(if (localNetwork.granted) R.string.allowed else R.string.not_allowed)

    AppScaffold(
        onNavigateBack = { navController.navigateBack() },
        modifier = modifier,
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = stringResource(R.string.port_forwarding),
        content = { innerPadding, topBarScrollBehavior ->
            SettingsColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                listState = listState,
                contentPadding = innerPadding,
                topAppBarState = topAppBarState,
                hapticsEnabled = hapticsEnabled,
            ) {
                group(R.string.startup) {
                    switchItem(SettingsKeys.PortForwardAutoStart) {
                        title(R.string.port_forward_auto_start)
                        description(R.string.des_port_forward_auto_start)
                        icon(R.drawable.ic_play)
                    }
                }

                group(R.string.port_forward_group_device_disconnects) {
                    switchItem(SettingsKeys.PortForwardResumeOnReconnect) {
                        title(R.string.port_forward_resume_on_reconnect)
                        description(R.string.des_port_forward_resume_on_reconnect)
                        icon(R.drawable.ic_otg)
                    }

                    clickableItem(SettingsKeys.PortForwardWaitTimeoutMinutes) {
                        title(R.string.port_forward_wait_timeout)
                        description { waitTimeoutLabel(waitTimeout) }
                        icon(R.drawable.ic_schedule)
                        enabled { resumeOnReconnect }
                        onClick { viewModel.showDialog(PortForwardSettingsDialog.WaitTimeout) }
                    }
                }

                group(R.string.network) {
                    switchItem(SettingsKeys.PortForwardAllowLanSharing) {
                        title(R.string.port_forward_allow_lan_sharing)
                        description(R.string.des_port_forward_allow_lan_sharing)
                        icon(R.drawable.ic_wifi_settings)
                        onClick { viewModel.onLanSharingClick(lanSharingAllowed) }
                    }

                    clickableItem(SettingsKeys.PortForwardConnectTimeoutSeconds) {
                        title(R.string.port_forward_connect_timeout)
                        description { pluralStringResource(R.plurals.n_seconds, connectTimeout, connectTimeout) }
                        icon(R.drawable.ic_schedule)
                        onClick { viewModel.showDialog(PortForwardSettingsDialog.ConnectTimeout) }
                    }

                    clickableItem(SettingsKeys.PortForwardMaxConnections) {
                        title(R.string.port_forward_max_connections)
                        description { maxConnections.toString() }
                        icon(R.drawable.ic_port_forward)
                        onClick { viewModel.showDialog(PortForwardSettingsDialog.MaxConnections) }
                    }

                    clickableItem(SettingsKeys.PortForwardLocalNetworkAccess) {
                        title(R.string.port_forward_local_network_access)
                        description { localNetworkStatus }
                        icon(R.drawable.ic_shield)
                        visible { LocalNetworkPermission.isRequired }
                        onClick { localNetwork.openSettings() }
                    }
                }

                group(R.string.port_forward_group_background) {
                    switchItem(SettingsKeys.PortForwardStopOnTaskRemoved) {
                        title(R.string.port_forward_stop_on_task_removed)
                        description(R.string.des_port_forward_stop_on_task_removed)
                        icon(R.drawable.ic_stop)
                    }

                    switchItem(SettingsKeys.PortForwardUnexpectedStopAlerts) {
                        title(R.string.port_forward_unexpected_stop_alerts)
                        description(R.string.des_port_forward_unexpected_stop_alerts)
                        icon(R.drawable.ic_notifications_important)
                    }

                    clickableItem(SettingsKeys.PortForwardNotificationSettings) {
                        title(R.string.notification_settings)
                        description(R.string.des_port_forward_notification_settings)
                        icon(R.drawable.ic_notifications)
                        onClick { context.startActivity(channelSettingsIntent(context)) }
                    }
                }

                group(R.string.data) {
                    clickableItem(SettingsKeys.PortForwardClearRecentHosts) {
                        title(R.string.port_forward_clear_recent_hosts)
                        description(R.string.des_port_forward_clear_recent_hosts)
                        icon(R.drawable.ic_delete_sweep)
                        onClick { viewModel.showDialog(PortForwardSettingsDialog.ClearRecentHosts) }
                    }
                }

                item(key = "spacer_bottom") {
                    Spacer(modifier = Modifier.fillMaxWidth().height(30.dp))
                }
            }
        }
    )

    dialog?.let { current ->
        SettingsDialog(current, SettingsChoices(connectTimeout, maxConnections, waitTimeout), viewModel)
    }
}

/** The current values of the settings chosen in dialogs. */
private data class SettingsChoices(val connectTimeout: Int, val maxConnections: Int, val waitTimeout: Int)

@Composable
private fun waitTimeoutLabel(minutes: Int): String =
    if (minutes == PortForwardDefaults.WAIT_FOREVER) {
        stringResource(R.string.port_forward_wait_forever)
    } else {
        pluralStringResource(R.plurals.timeout_minutes, minutes, minutes)
    }

@Composable
private fun SettingsDialog(
    dialog: PortForwardSettingsDialog,
    choices: SettingsChoices,
    viewModel: PortForwardSettingsViewModel
) {
    when (dialog) {
        PortForwardSettingsDialog.WaitTimeout -> ChoiceDialog(
            title = stringResource(R.string.port_forward_wait_timeout),
            options = PortForwardDefaults.WAIT_TIMEOUT_OPTIONS,
            selected = choices.waitTimeout,
            label = { waitTimeoutLabel(it) },
            onApply = viewModel::setWaitTimeout,
            onDismiss = viewModel::dismissDialog
        )

        PortForwardSettingsDialog.EnableLanSharing -> ConfirmDialog(
            title = stringResource(R.string.port_forward_enable_sharing_title),
            message = stringResource(R.string.port_forward_enable_sharing_message),
            confirmLabel = stringResource(R.string.allow),
            onConfirm = viewModel::confirmEnableLanSharing,
            onDismiss = viewModel::dismissDialog
        )

        is PortForwardSettingsDialog.DisableLanSharing -> ConfirmDialog(
            title = stringResource(R.string.port_forward_disable_sharing_title),
            message = pluralStringResource(
                R.plurals.port_forward_disable_sharing_message,
                dialog.sharedRunningCount,
                dialog.sharedRunningCount
            ),
            confirmLabel = stringResource(R.string.turn_off),
            onConfirm = viewModel::confirmDisableLanSharing,
            onDismiss = viewModel::dismissDialog
        )

        PortForwardSettingsDialog.ConnectTimeout -> ChoiceDialog(
            title = stringResource(R.string.port_forward_connect_timeout),
            options = PortForwardDefaults.CONNECT_TIMEOUT_OPTIONS,
            selected = choices.connectTimeout,
            label = { pluralStringResource(R.plurals.n_seconds, it, it) },
            onApply = viewModel::setConnectTimeout,
            onDismiss = viewModel::dismissDialog
        )

        PortForwardSettingsDialog.MaxConnections -> ChoiceDialog(
            title = stringResource(R.string.port_forward_max_connections),
            options = PortForwardDefaults.MAX_CONNECTIONS_OPTIONS,
            selected = choices.maxConnections,
            label = { it.toString() },
            onApply = viewModel::setMaxConnections,
            onDismiss = viewModel::dismissDialog
        )

        PortForwardSettingsDialog.ClearRecentHosts -> ConfirmDialog(
            title = stringResource(R.string.port_forward_clear_recent_hosts),
            message = stringResource(R.string.des_port_forward_clear_recent_hosts_confirm),
            confirmLabel = stringResource(R.string.clear),
            onConfirm = viewModel::clearRecentHosts,
            onDismiss = viewModel::dismissDialog
        )
    }
}

private fun channelSettingsIntent(context: Context): Intent =
    Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .putExtra(Settings.EXTRA_CHANNEL_ID, NotificationChannelManager.CHANNEL_PORT_FORWARDING)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
