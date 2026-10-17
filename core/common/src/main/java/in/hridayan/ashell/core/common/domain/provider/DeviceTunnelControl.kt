package `in`.hridayan.ashell.core.common.domain.provider

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport

/**
 * Stops the port-forwarding tunnels that reach a device, for features that end a device connection
 * on the user's request. Tunnels stopped this way do not wait for the device to return.
 */
interface DeviceTunnelControl {
    suspend fun stopTunnels(transport: ExternalDeviceTransport)
}
