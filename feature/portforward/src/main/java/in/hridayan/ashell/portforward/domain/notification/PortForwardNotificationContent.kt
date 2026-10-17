package `in`.hridayan.ashell.portforward.domain.notification

import `in`.hridayan.ashell.portforward.domain.model.TunnelRuntime
import `in`.hridayan.ashell.portforward.domain.model.TunnelState
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.WaitReason
import `in`.hridayan.ashell.portforward.domain.model.browserUrl
import `in`.hridayan.ashell.portforward.domain.model.isRunning
import `in`.hridayan.ashell.portforward.domain.model.localAddress
import `in`.hridayan.ashell.portforward.domain.model.targetAddress

/** How a tunnel is doing, in the terms the notification shows. */
enum class NotificationTunnelStatus {
    Starting,
    Ready,
    Active,
    NeedsAttention,
    WaitingForNetwork,
    WaitingForDevice
}

data class NotificationTunnelLine(val name: String, val status: NotificationTunnelStatus)

/**
 * What the ongoing notification shows, independent of how Android renders it.
 *
 * Counters are left out on purpose. They change with every request, and including them would force
 * a rebuild of the notification for each one.
 */
sealed interface PortForwardNotificationContent {

    /**
     * @property openUrl the address to open in a browser, or null when the target is not a website.
     * @property deviceName the device a device tunnel reaches, or null for a network tunnel.
     * @property waitDeadlineMillis when a waiting tunnel gives up, for a countdown, or null.
     */
    data class Single(
        val tunnelId: String,
        val name: String,
        val localAddress: String,
        val targetAddress: String,
        val status: NotificationTunnelStatus,
        val openUrl: String?,
        val deviceName: String? = null,
        val waitDeadlineMillis: Long? = null
    ) : PortForwardNotificationContent

    data class Multiple(
        val runningCount: Int,
        val waitingCount: Int,
        val attentionCount: Int,
        val lines: List<NotificationTunnelLine>,
        val hiddenCount: Int
    ) : PortForwardNotificationContent

    companion object {
        const val MAX_LINES = 5

        /** Null when no tunnel is running, so there is nothing to show. */
        fun from(runtimes: Collection<TunnelRuntime>): PortForwardNotificationContent? {
            val running = runtimes.filter { it.state.isRunning }
            return when (running.size) {
                0 -> null
                1 -> single(running.single())
                else -> multiple(running)
            }
        }

        private fun single(runtime: TunnelRuntime) = Single(
            tunnelId = runtime.spec.id,
            name = runtime.spec.name,
            localAddress = runtime.spec.localAddress,
            targetAddress = runtime.spec.targetAddress,
            status = runtime.state.toNotificationStatus(),
            openUrl = runtime.spec.browserUrl.takeIf { runtime.spec.looksLikeHttp && runtime.state.isRunning },
            deviceName = (runtime.spec.target as? TunnelTarget.Device)?.deviceName,
            waitDeadlineMillis = (runtime.state as? TunnelState.Waiting)?.deadlineMillis
        )

        private fun multiple(running: List<TunnelRuntime>): Multiple {
            val statuses = running.map { it.state.toNotificationStatus() }
            return Multiple(
                runningCount = running.size,
                waitingCount = statuses.count { it in WAITING_STATUSES },
                attentionCount = statuses.count { it == NotificationTunnelStatus.NeedsAttention },
                lines = running.take(MAX_LINES).map {
                    NotificationTunnelLine(it.spec.name, it.state.toNotificationStatus())
                },
                hiddenCount = (running.size - MAX_LINES).coerceAtLeast(0)
            )
        }

        private fun TunnelState.toNotificationStatus(): NotificationTunnelStatus = when (this) {
            TunnelState.Starting -> NotificationTunnelStatus.Starting
            TunnelState.Ready -> NotificationTunnelStatus.Ready
            TunnelState.Active -> NotificationTunnelStatus.Active
            is TunnelState.Warning, is TunnelState.Failed -> NotificationTunnelStatus.NeedsAttention
            is TunnelState.Waiting -> when (reason) {
                WaitReason.Network -> NotificationTunnelStatus.WaitingForNetwork
                WaitReason.Device -> NotificationTunnelStatus.WaitingForDevice
            }
        }

        private val WAITING_STATUSES = setOf(
            NotificationTunnelStatus.WaitingForNetwork,
            NotificationTunnelStatus.WaitingForDevice
        )
    }
}
