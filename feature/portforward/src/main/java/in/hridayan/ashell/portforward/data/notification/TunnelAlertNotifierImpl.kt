package `in`.hridayan.ashell.portforward.data.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.notification.NotificationChannelManager
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.utils.isNotificationPermissionGranted
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.notification.TunnelAlertNotifier
import `in`.hridayan.ashell.portforward.domain.repository.PortForwardSettingsSource
import `in`.hridayan.ashell.portforward.service.PortForwardIntents
import `in`.hridayan.ashell.portforward.text.message
import `in`.hridayan.ashell.portforward.text.resolve
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Posts an alert for a tunnel that failed while the app was in the background.
 *
 * Each tunnel has one alert id, so a tunnel that fails again replaces its earlier alert instead of
 * stacking another.
 */
class TunnelAlertNotifierImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settings: PortForwardSettingsSource
) : TunnelAlertNotifier {

    override suspend fun tunnelFailed(spec: TunnelSpec, error: TunnelError) {
        if (isAppVisible() || !settings.current().unexpectedStopAlerts) return
        if (!isNotificationPermissionGranted(context)) return
        val notification = NotificationCompat.Builder(
            context,
            NotificationChannelManager.CHANNEL_PORT_FORWARDING_ALERTS
        )
            .setSmallIcon(R.drawable.ic_port_forward)
            .setContentTitle(context.getString(R.string.port_forward_alert_stopped, spec.name))
            .setContentText(context.resolve(error.message(spec)))
            .setContentIntent(PortForwardIntents.openPorts(context))
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            ?.notify(ALERT_ID_BASE + spec.id.hashCode(), notification)
    }

    private suspend fun isAppVisible(): Boolean = withContext(Dispatchers.Main) {
        ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    private companion object {
        const val ALERT_ID_BASE = 2_100
    }
}
