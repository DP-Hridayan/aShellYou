package `in`.hridayan.ashell.portforward.presentation.model

import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpoint
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.allowsNetworkSharing

private const val NAME_SEPARATOR = " "

/** The form as a [TunnelSpec], or null while a required number is not a number. */
fun TunnelEditorForm.toSpecOrNull(id: String): TunnelSpec? {
    val local = localPort.trim().toIntOrNull() ?: return null
    val target = targetOrNull() ?: return null
    return TunnelSpec(
        id = id,
        name = name.trim().ifEmpty { defaultName(target) },
        localPort = local,
        shareOnNetwork = shareOnNetwork && target.allowsNetworkSharing,
        target = target,
        autoStart = autoStart,
        looksLikeHttp = looksLikeHttp
    )
}

/** The target the form describes, or null while it is incomplete. */
fun TunnelEditorForm.targetOrNull(): TunnelTarget? = when (val currentKind = kind) {
    EditorKind.Network -> targetPort.trim().toIntOrNull()?.let { port ->
        TunnelTarget.NetworkHost(host = host.trim(), port = port, path = path)
    }

    is EditorKind.Device -> endpointValue.trim().takeIf { it.isNotEmpty() }?.let { value ->
        TunnelTarget.Device(
            transport = currentKind.device.transport,
            deviceId = currentKind.device.deviceId,
            deviceName = currentKind.device.deviceName,
            endpoint = DeviceEndpoint(endpointType, value)
        )
    }
}

fun TunnelSpec.toForm(): TunnelEditorForm = when (val target = target) {
    is TunnelTarget.NetworkHost -> TunnelEditorForm(
        host = target.host,
        targetPort = target.port.toString(),
        path = target.path,
        localPort = localPort.toString(),
        sameAsTarget = localPort == target.port,
        name = name,
        autoStart = autoStart,
        shareOnNetwork = shareOnNetwork,
        looksLikeHttp = looksLikeHttp
    )

    is TunnelTarget.Device -> TunnelEditorForm(
        kind = EditorKind.Device(EditorDevice(target.transport, target.deviceId, target.deviceName)),
        endpointType = target.endpoint.type,
        endpointValue = target.endpoint.value,
        localPort = localPort.toString(),
        sameAsTarget = target.endpoint.value == localPort.toString(),
        name = name,
        autoStart = autoStart,
        shareOnNetwork = shareOnNetwork,
        looksLikeHttp = looksLikeHttp
    )
}

private fun defaultName(target: TunnelTarget): String = when (target) {
    is TunnelTarget.NetworkHost -> "${target.host}:${target.port}"
    is TunnelTarget.Device -> listOf(target.deviceName, target.endpoint.value).joinToString(NAME_SEPARATOR)
}
