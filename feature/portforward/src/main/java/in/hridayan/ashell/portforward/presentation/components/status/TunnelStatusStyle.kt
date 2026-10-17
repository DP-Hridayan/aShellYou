package `in`.hridayan.ashell.portforward.presentation.components.status

import android.content.Context
import android.text.format.Formatter
import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.domain.model.TunnelEventKind
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelState
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.WaitReason
import `in`.hridayan.ashell.portforward.text.TextRes
import `in`.hridayan.ashell.portforward.text.message
import `in`.hridayan.ashell.portforward.text.resolve

/** The icon and colour role a tunnel state is shown with. Every state also has a text label. */
data class TunnelStatusStyle(@param:DrawableRes val icon: Int, val color: Color)

@Composable
@ReadOnlyComposable
fun TunnelState.style(): TunnelStatusStyle = when (this) {
    TunnelState.Starting -> TunnelStatusStyle(R.drawable.ic_schedule, MaterialTheme.colorScheme.secondary)
    TunnelState.Ready, TunnelState.Active -> TunnelStatusStyle(
        R.drawable.ic_check_circle,
        MaterialTheme.colorScheme.primary
    )
    is TunnelState.Warning -> TunnelStatusStyle(R.drawable.ic_warning, MaterialTheme.colorScheme.tertiary)
    is TunnelState.Waiting -> TunnelStatusStyle(
        if (reason == WaitReason.Device) R.drawable.ic_schedule else R.drawable.ic_no_wifi,
        MaterialTheme.colorScheme.secondary
    )
    is TunnelState.Failed -> TunnelStatusStyle(R.drawable.ic_error, MaterialTheme.colorScheme.error)
}

/** A short label: the state name, or the cause of a warning or failure. */
fun TunnelState.label(spec: TunnelSpec): TextRes = when (this) {
    TunnelState.Starting -> TextRes(R.string.port_forward_status_starting)
    TunnelState.Ready -> TextRes(R.string.port_forward_status_ready)
    TunnelState.Active -> TextRes(R.string.active)
    is TunnelState.Warning -> error.message(spec)
    is TunnelState.Waiting -> when (reason) {
        WaitReason.Network -> TextRes(R.string.port_forward_status_waiting_for_network)
        WaitReason.Device -> TextRes(
            R.string.port_forward_waiting_for_device,
            listOf((spec.target as? TunnelTarget.Device)?.deviceName.orEmpty())
        )
    }
    is TunnelState.Failed -> error.message(spec)
}

/** One line of the activity log. */
fun TunnelEventKind.describe(context: Context, spec: TunnelSpec): String = when (this) {
    TunnelEventKind.Started -> context.getString(R.string.port_forward_event_started)
    TunnelEventKind.ConnectionOpened -> context.getString(R.string.port_forward_event_connection_opened)
    is TunnelEventKind.ConnectionClosed -> context.getString(
        R.string.port_forward_event_connection_closed,
        Formatter.formatShortFileSize(context, bytesReceived),
        Formatter.formatShortFileSize(context, bytesSent)
    )

    is TunnelEventKind.ConnectionFailed ->
        context.getString(R.string.port_forward_event_connection_failed, context.resolve(error.message(spec)))

    is TunnelEventKind.ConnectionRefused ->
        context.getString(R.string.port_forward_event_connection_refused, context.resolve(error.message(spec)))

    TunnelEventKind.WaitingForNetwork -> context.getString(R.string.port_forward_status_waiting_for_network)
    TunnelEventKind.NetworkRestored -> context.getString(R.string.port_forward_event_network_restored)
    TunnelEventKind.WaitingForDevice -> context.getString(
        R.string.port_forward_waiting_for_device,
        (spec.target as? TunnelTarget.Device)?.deviceName.orEmpty()
    )
    TunnelEventKind.DeviceReconnected -> context.getString(R.string.port_forward_event_device_reconnected)
}
