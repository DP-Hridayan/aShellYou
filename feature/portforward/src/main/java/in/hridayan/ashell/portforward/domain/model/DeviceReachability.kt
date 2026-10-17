package `in`.hridayan.ashell.portforward.domain.model

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState

/**
 * True when this state is a connection to [target]'s device. A device that reports no serial number
 * cannot be told apart from another, so it is accepted; a physical replug or a user reconnect is a
 * strong enough signal on its own.
 */
fun ExternalDeviceState.reaches(target: TunnelTarget.Device): Boolean {
    val connected = this as? ExternalDeviceState.Connected ?: return false
    return connected.deviceId == null || target.deviceId == null || connected.deviceId == target.deviceId
}
