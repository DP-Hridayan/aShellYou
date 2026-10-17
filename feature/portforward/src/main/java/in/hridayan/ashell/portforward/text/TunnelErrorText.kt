package `in`.hridayan.ashell.portforward.text

import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget

/** One sentence that names the cause of [this] error for [spec]. */
fun TunnelError.message(spec: TunnelSpec): TextRes = when (this) {
    is TunnelError.PortInUse -> TextRes(R.string.port_forward_error_port_in_use, listOf(spec.localPort))
    TunnelError.PortPrivileged -> TextRes(R.string.port_forward_error_port_privileged)
    TunnelError.LocalNetworkBlocked -> TextRes(R.string.port_forward_error_local_network_blocked)
    TunnelError.BackgroundStartBlocked -> TextRes(R.string.port_forward_error_background_start)
    TunnelError.NoNetwork -> TextRes(R.string.port_forward_error_no_network)
    TunnelError.TargetRefused -> TextRes(R.string.port_forward_error_target_refused)
    TunnelError.TargetTimeout -> TextRes(R.string.port_forward_error_target_timeout)
    TunnelError.HostUnresolved -> TextRes(R.string.port_forward_error_host_unresolved, listOf(spec.hostName()))
    TunnelError.StreamCapReached -> TextRes(R.string.port_forward_error_too_many_connections)
    TunnelError.NothingListeningOnDevice -> TextRes(
        R.string.port_forward_error_nothing_listening,
        listOf(spec.endpointLabel())
    )
    TunnelError.DeviceOpenTimeout -> TextRes(R.string.port_forward_error_device_timeout, listOf(spec.deviceLabel()))
    TunnelError.DeviceNotConnected -> TextRes(
        R.string.port_forward_error_device_not_connected,
        listOf(spec.deviceLabel())
    )
    TunnelError.DeviceDidNotReturn -> TextRes(
        R.string.port_forward_error_device_did_not_return,
        listOf(spec.deviceLabel())
    )
    is TunnelError.Unexpected -> message?.let { TextRes(R.string.port_forward_error_unexpected, listOf(it)) }
        ?: TextRes(R.string.port_forward_error_unexpected_unknown)
}

private fun TunnelSpec.hostName(): String = when (val target = target) {
    is TunnelTarget.NetworkHost -> target.host
    is TunnelTarget.Device -> target.deviceName
}

private fun TunnelSpec.deviceLabel(): String = (target as? TunnelTarget.Device)?.deviceName.orEmpty()

private fun TunnelSpec.endpointLabel(): String = (target as? TunnelTarget.Device)?.endpoint?.value.orEmpty()
