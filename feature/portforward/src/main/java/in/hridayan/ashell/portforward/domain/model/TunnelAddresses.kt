package `in`.hridayan.ashell.portforward.domain.model

private const val LOCALHOST = "localhost"
private const val HTTPS_PORT = 443
private const val HTTP_SCHEME = "http"
private const val HTTPS_SCHEME = "https"
private const val DEVICE_SEPARATOR = " · "

/** The address clients on this phone connect to, such as `localhost:5173`. */
val TunnelSpec.localAddress: String
    get() = "$LOCALHOST:$localPort"

/**
 * Where the tunnel's traffic goes, such as `192.168.1.10:5173`, `Pixel 7:9222` or
 * `Pixel 7 · chrome_devtools_remote`.
 */
val TunnelSpec.targetAddress: String
    get() = when (val target = target) {
        is TunnelTarget.NetworkHost -> "${target.host.bracketedIfIpv6()}:${target.port}"
        is TunnelTarget.Device -> target.address()
    }

/** The URL that opens the tunnel's target in a browser on this phone. */
val TunnelSpec.browserUrl: String
    get() {
        val path = when (val target = target) {
            is TunnelTarget.NetworkHost -> target.path.orEmpty()
            is TunnelTarget.Device -> ""
        }
        return "${scheme()}://$localAddress$path"
    }

/** The same as [browserUrl], reached from another device at [hostAddress]. */
fun TunnelSpec.sharedUrl(hostAddress: String): String = browserUrl.replaceFirst(LOCALHOST, hostAddress)

private fun TunnelTarget.Device.address(): String = when (endpoint.type) {
    DeviceEndpointType.TCP -> "$deviceName:${endpoint.value}"
    else -> "$deviceName$DEVICE_SEPARATOR${endpoint.value}"
}

private fun TunnelSpec.scheme(): String = when (val target = target) {
    is TunnelTarget.NetworkHost -> if (target.port == HTTPS_PORT) HTTPS_SCHEME else HTTP_SCHEME
    is TunnelTarget.Device -> HTTP_SCHEME
}

private fun String.bracketedIfIpv6(): String = if (contains(':')) "[$this]" else this
