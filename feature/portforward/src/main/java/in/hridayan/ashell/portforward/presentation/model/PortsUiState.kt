package `in`.hridayan.ashell.portforward.presentation.model

import androidx.compose.runtime.Immutable
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.portforward.domain.model.TunnelRuntime
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.isRunning
import `in`.hridayan.ashell.portforward.domain.model.reaches

/**
 * A saved tunnel with its live state, or no state when it is stopped.
 *
 * @property deviceState the state of the transport a device tunnel uses, or null for a network
 * tunnel.
 */
@Immutable
data class TunnelItem(
    val spec: TunnelSpec,
    val runtime: TunnelRuntime?,
    val deviceState: ExternalDeviceState? = null
) {
    val isRunning: Boolean
        get() = runtime?.state?.isRunning == true

    /** False for a device tunnel whose device is not connected, which cannot start. */
    val canStart: Boolean
        get() {
            val device = spec.target as? TunnelTarget.Device ?: return true
            return deviceState?.reaches(device) == true
        }

    /** A different device now connected on this tunnel's transport, which the tunnel could move to. */
    val otherConnectedDevice: ExternalDeviceState.Connected?
        get() {
            val device = spec.target as? TunnelTarget.Device ?: return null
            val connected = deviceState as? ExternalDeviceState.Connected ?: return null
            return connected.takeUnless { it.reaches(device) }
        }
}

/** Which tunnels the list shows. */
sealed interface PortsScope {
    data object All : PortsScope
    data object Network : PortsScope
    data class Device(val transport: ExternalDeviceTransport) : PortsScope
}

/**
 * @property interrupted tunnels the system stopped while they were running.
 * @property devices the connection state of each transport.
 */
@Immutable
data class PortsUiState(
    val isLoaded: Boolean = false,
    val scope: PortsScope = PortsScope.All,
    val running: List<TunnelItem> = emptyList(),
    val saved: List<TunnelItem> = emptyList(),
    val interrupted: List<TunnelSpec> = emptyList(),
    val devices: Map<ExternalDeviceTransport, ExternalDeviceState> = emptyMap()
) {
    val isEmpty: Boolean
        get() = isLoaded && running.isEmpty() && saved.isEmpty()

    val hasRunning: Boolean
        get() = running.isNotEmpty()

    val connectedDevices: Map<ExternalDeviceTransport, ExternalDeviceState.Connected>
        get() = devices.mapNotNull { (transport, state) ->
            (state as? ExternalDeviceState.Connected)?.let { transport to it }
        }.toMap()

    /** The connected device of the current device scope, or null. */
    val scopedDevice: ExternalDeviceState.Connected?
        get() = (scope as? PortsScope.Device)?.let { connectedDevices[it.transport] }

    fun item(id: String): TunnelItem? =
        running.firstOrNull { it.spec.id == id } ?: saved.firstOrNull { it.spec.id == id }
}

sealed interface PortsEvent {
    data class Started(val spec: TunnelSpec) : PortsEvent
    data class Deleted(val spec: TunnelSpec, val wasRunning: Boolean) : PortsEvent
    data class StoppedAll(val specs: List<TunnelSpec>) : PortsEvent
}
