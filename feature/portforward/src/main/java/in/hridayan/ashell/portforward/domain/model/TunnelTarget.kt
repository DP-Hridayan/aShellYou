package `in`.hridayan.ashell.portforward.domain.model

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport

/** Where a tunnel sends the traffic it accepts on this phone. */
sealed interface TunnelTarget {

    /**
     * A TCP port on another machine, reached over the phone's network.
     *
     * @property path the path to open in a browser, starting with a slash, or null for the root.
     */
    data class NetworkHost(val host: String, val port: Int, val path: String? = null) : TunnelTarget

    /**
     * A socket on a device connected over USB or Wireless Debugging, reached through ADB.
     *
     * @property deviceId the device's serial number when the tunnel was made, or null when it did not
     * report one. Waiting tunnels resume only on a device with the same id.
     */
    data class Device(
        val transport: ExternalDeviceTransport,
        val deviceId: String?,
        val deviceName: String,
        val endpoint: DeviceEndpoint
    ) : TunnelTarget
}

/** A socket on the device, named the way ADB's forward services name it. */
data class DeviceEndpoint(val type: DeviceEndpointType, val value: String) {
    val service: String
        get() = type.servicePrefix + value
}

enum class DeviceEndpointType(val servicePrefix: String) {
    TCP("tcp:"),
    ABSTRACT("localabstract:"),
    FILESYSTEM("localfilesystem:"),
    JDWP("jdwp:")
}

/**
 * Whether the tunnel may listen on the Wi-Fi network. Sockets that give full control of a process,
 * such as debugger and instrumentation sockets, are never shared.
 */
val TunnelTarget.allowsNetworkSharing: Boolean
    get() = when (this) {
        is TunnelTarget.NetworkHost -> true
        is TunnelTarget.Device ->
            endpoint.type == DeviceEndpointType.TCP &&
                endpoint.value != DevicePreset.FRIDA.endpoint.value
    }
