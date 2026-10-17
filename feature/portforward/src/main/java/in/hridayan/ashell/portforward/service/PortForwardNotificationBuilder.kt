package `in`.hridayan.ashell.portforward.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import `in`.hridayan.ashell.core.common.notification.NotificationChannelManager
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.domain.notification.NotificationTunnelStatus
import `in`.hridayan.ashell.portforward.domain.notification.PortForwardNotificationContent

/** Renders [PortForwardNotificationContent] as the ongoing status notification. */
class PortForwardNotificationBuilder(private val context: Context) {

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    /** @param content null while the first tunnel is still starting. */
    fun build(content: PortForwardNotificationContent?): Notification {
        val builder = baseBuilder()
        when (content) {
            null -> builder.setContentTitle(context.getString(R.string.port_forward_starting))
            is PortForwardNotificationContent.Single -> builder.applySingle(content)
            is PortForwardNotificationContent.Multiple -> builder.applyMultiple(content)
        }
        return builder.build()
    }

    fun update(content: PortForwardNotificationContent?) {
        notificationManager?.notify(NOTIFICATION_ID, build(content))
    }

    private fun baseBuilder(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_PORT_FORWARDING)
            .setSmallIcon(R.drawable.ic_port_forward)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(PortForwardIntents.openPorts(context))

    private fun NotificationCompat.Builder.applySingle(content: PortForwardNotificationContent.Single) {
        if (content.status == NotificationTunnelStatus.WaitingForDevice) {
            applyWaitingForDevice(content)
            return
        }
        setContentTitle(content.name)
        setContentText(
            context.getString(
                R.string.port_forward_notification_single,
                content.localAddress,
                content.targetAddress,
                statusLabel(content.status)
            )
        )
        content.openUrl?.let { url ->
            addAction(
                R.drawable.ic_open_in_new,
                context.getString(R.string.open_in_browser),
                PortForwardIntents.openInBrowser(context, url)
            )
        }
        addAction(
            R.drawable.ic_cancel,
            context.getString(R.string.stop),
            PortForwardIntents.stop(context, content.tunnelId)
        )
    }

    private fun NotificationCompat.Builder.applyWaitingForDevice(content: PortForwardNotificationContent.Single) {
        setContentTitle(context.getString(R.string.port_forward_waiting_for_device, content.deviceName.orEmpty()))
        setContentText(context.getString(R.string.port_forward_resumes_when_reconnected, content.name))
        content.waitDeadlineMillis?.let { deadline ->
            setWhen(deadline)
            setShowWhen(true)
            setUsesChronometer(true)
            setChronometerCountDown(true)
        }
        addAction(
            R.drawable.ic_cancel,
            context.getString(R.string.stop),
            PortForwardIntents.stop(context, content.tunnelId)
        )
    }

    private fun NotificationCompat.Builder.applyMultiple(content: PortForwardNotificationContent.Multiple) {
        val title = quantity(R.plurals.port_forward_tunnels_running, content.runningCount)
        val summary = summaryText(content)
        setContentTitle(title)
        setContentText(summary)
        setStyle(inboxStyle(content, summary))
        addAction(R.drawable.ic_cancel, context.getString(R.string.stop_all), PortForwardIntents.stopAll(context))
    }

    private fun inboxStyle(
        content: PortForwardNotificationContent.Multiple,
        summary: String
    ): NotificationCompat.InboxStyle {
        val style = NotificationCompat.InboxStyle().setSummaryText(summary)
        content.lines.forEach { line ->
            style.addLine(
                context.getString(R.string.port_forward_notification_line, line.name, statusLabel(line.status))
            )
        }
        if (content.hiddenCount > 0) {
            style.addLine(context.getString(R.string.port_forward_notification_more, content.hiddenCount))
        }
        return style
    }

    private fun summaryText(content: PortForwardNotificationContent.Multiple): String {
        val parts = buildList {
            if (content.waitingCount > 0) {
                add(quantity(R.plurals.port_forward_notification_waiting_count, content.waitingCount))
            }
            if (content.attentionCount > 0) {
                add(quantity(R.plurals.port_forward_notification_attention_count, content.attentionCount))
            }
        }
        return parts.joinToString(SUMMARY_SEPARATOR).ifEmpty {
            context.getString(R.string.port_forward_notification_tap_to_manage)
        }
    }

    private fun quantity(id: Int, count: Int): String = context.resources.getQuantityString(id, count, count)

    private fun statusLabel(status: NotificationTunnelStatus): String = context.getString(
        when (status) {
            NotificationTunnelStatus.Starting -> R.string.port_forward_status_starting
            NotificationTunnelStatus.Ready -> R.string.port_forward_status_ready
            NotificationTunnelStatus.Active -> R.string.active
            NotificationTunnelStatus.NeedsAttention -> R.string.needs_attention
            NotificationTunnelStatus.WaitingForNetwork -> R.string.port_forward_status_waiting_for_network
            NotificationTunnelStatus.WaitingForDevice -> R.string.port_forward_status_waiting_for_device
        }
    )

    companion object {
        const val NOTIFICATION_ID = 2005
        private const val SUMMARY_SEPARATOR = " · "
    }
}
