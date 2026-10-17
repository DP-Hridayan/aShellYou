package `in`.hridayan.ashell.portforward.domain

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpoint
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.model.TunnelRuntime
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelState
import `in`.hridayan.ashell.portforward.domain.model.TunnelStats
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget

fun spec(
    id: String,
    name: String = "Tunnel $id",
    localPort: Int = 5173,
    host: String = "192.168.1.10",
    port: Int = 5173,
    path: String? = null,
    looksLikeHttp: Boolean = true
) = TunnelSpec(
    id = id,
    name = name,
    localPort = localPort,
    shareOnNetwork = false,
    target = TunnelTarget.NetworkHost(host, port, path),
    autoStart = false,
    looksLikeHttp = looksLikeHttp
)

fun deviceSpec(
    id: String,
    transport: ExternalDeviceTransport,
    endpoint: DeviceEndpoint = DeviceEndpoint(DeviceEndpointType.TCP, "9222"),
    deviceId: String? = "serial-1"
) = TunnelSpec(
    id = id,
    name = "Device $id",
    localPort = 9222,
    shareOnNetwork = false,
    target = TunnelTarget.Device(transport, deviceId, "Pixel 7", endpoint),
    autoStart = false,
    looksLikeHttp = false
)

fun runtime(spec: TunnelSpec, state: TunnelState = TunnelState.Ready) =
    TunnelRuntime(spec = spec, state = state, stats = TunnelStats(), events = emptyList())
