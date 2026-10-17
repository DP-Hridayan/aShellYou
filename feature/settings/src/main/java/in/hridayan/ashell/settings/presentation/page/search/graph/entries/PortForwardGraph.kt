package `in`.hridayan.ashell.settings.presentation.page.search.graph.entries

import androidx.navigation.NavController
import `in`.hridayan.ashell.core.common.network.LocalNetworkPermission
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.page.search.graph.constants.SettingsGraphId
import `in`.hridayan.settingsgraph.search.SearchScreenScope

internal fun SearchScreenScope.portForwardGraph(navController: NavController) {
    screen(
        id = SettingsGraphId.PORT_FORWARD,
        title = R.string.port_forwarding,
        navigate = { navController.navigate(NavRoutes.PortForwardSettingsScreen) { launchSingleTop = true } },
    ) {
        entry(key = SettingsKeys.PortForwardAutoStart) {
            title(R.string.port_forward_auto_start)
            description(R.string.des_port_forward_auto_start)
            icon(R.drawable.ic_play)
        }

        entry(key = SettingsKeys.PortForwardResumeOnReconnect) {
            title(R.string.port_forward_resume_on_reconnect)
            description(R.string.des_port_forward_resume_on_reconnect)
            icon(R.drawable.ic_otg)
        }

        entry(key = SettingsKeys.PortForwardWaitTimeoutMinutes) {
            title(R.string.port_forward_wait_timeout)
            icon(R.drawable.ic_schedule)
        }

        entry(key = SettingsKeys.PortForwardAllowLanSharing) {
            title(R.string.port_forward_allow_lan_sharing)
            description(R.string.des_port_forward_allow_lan_sharing)
            icon(R.drawable.ic_wifi_settings)
        }

        entry(key = SettingsKeys.PortForwardConnectTimeoutSeconds) {
            title(R.string.port_forward_connect_timeout)
            icon(R.drawable.ic_schedule)
        }

        entry(key = SettingsKeys.PortForwardMaxConnections) {
            title(R.string.port_forward_max_connections)
            icon(R.drawable.ic_port_forward)
        }

        entry(key = SettingsKeys.PortForwardLocalNetworkAccess) {
            title(R.string.port_forward_local_network_access)
            icon(R.drawable.ic_shield)
            availableWhen(LocalNetworkPermission.isRequired)
        }

        entry(key = SettingsKeys.PortForwardStopOnTaskRemoved) {
            title(R.string.port_forward_stop_on_task_removed)
            description(R.string.des_port_forward_stop_on_task_removed)
            icon(R.drawable.ic_stop)
        }

        entry(key = SettingsKeys.PortForwardUnexpectedStopAlerts) {
            title(R.string.port_forward_unexpected_stop_alerts)
            description(R.string.des_port_forward_unexpected_stop_alerts)
            icon(R.drawable.ic_notifications_important)
        }

        entry(key = SettingsKeys.PortForwardNotificationSettings) {
            title(R.string.notification_settings)
            description(R.string.des_port_forward_notification_settings)
            icon(R.drawable.ic_notifications)
        }

        entry(key = SettingsKeys.PortForwardClearRecentHosts) {
            title(R.string.port_forward_clear_recent_hosts)
            description(R.string.des_port_forward_clear_recent_hosts)
            icon(R.drawable.ic_delete_sweep)
        }
    }
}
