package `in`.hridayan.ashell.portforward.data.local

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpoint
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.model.RecentHost
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget

internal const val KIND_NETWORK_HOST = "network_host"
internal const val KIND_DEVICE = "device"

/** The saved tunnel as a [TunnelSpec], or null when its kind is not known to this version. */
internal fun SavedTunnelEntity.toSpec(): TunnelSpec? {
    val target = when (kind) {
        KIND_NETWORK_HOST -> networkHostTarget()
        KIND_DEVICE -> deviceTarget()
        else -> null
    } ?: return null
    return TunnelSpec(
        id = id,
        name = name,
        localPort = localPort,
        shareOnNetwork = shareOnNetwork,
        target = target,
        autoStart = autoStart,
        looksLikeHttp = looksLikeHttp
    )
}

private fun SavedTunnelEntity.networkHostTarget(): TunnelTarget.NetworkHost? {
    val host = targetHost ?: return null
    val port = targetPort ?: return null
    return TunnelTarget.NetworkHost(host = host, port = port, path = targetPath)
}

private fun SavedTunnelEntity.deviceTarget(): TunnelTarget.Device? {
    val transport = transport?.let { name -> ExternalDeviceTransport.entries.firstOrNull { it.name == name } }
    val type = endpointType?.let { name -> DeviceEndpointType.entries.firstOrNull { it.name == name } }
    val value = endpointValue
    if (transport == null || type == null || value == null) return null
    return TunnelTarget.Device(
        transport = transport,
        deviceId = deviceId,
        deviceName = deviceName.orEmpty(),
        endpoint = DeviceEndpoint(type, value)
    )
}

/** @param existing the stored row being replaced, whose creation time and running flag are kept. */
internal fun TunnelSpec.toEntity(existing: SavedTunnelEntity?, now: Long): SavedTunnelEntity =
    when (val target = target) {
        is TunnelTarget.NetworkHost -> baseEntity(existing, now).copy(
            targetHost = target.host,
            targetPort = target.port,
            targetPath = target.path
        )

        is TunnelTarget.Device -> baseEntity(existing, now).copy(
            kind = KIND_DEVICE,
            transport = target.transport.name,
            deviceId = target.deviceId,
            deviceName = target.deviceName,
            endpointType = target.endpoint.type.name,
            endpointValue = target.endpoint.value
        )
    }

private fun TunnelSpec.baseEntity(existing: SavedTunnelEntity?, now: Long) = SavedTunnelEntity(
    id = id,
    name = name,
    kind = KIND_NETWORK_HOST,
    localPort = localPort,
    shareOnNetwork = shareOnNetwork,
    targetHost = null,
    targetPort = null,
    targetPath = null,
    autoStart = autoStart,
    looksLikeHttp = looksLikeHttp,
    wasRunning = existing?.wasRunning ?: false,
    createdAt = existing?.createdAt ?: now,
    updatedAt = now
)

internal fun RecentHostEntity.toDomain() = RecentHost(host = host, lastPort = lastPort)
