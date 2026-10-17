package `in`.hridayan.ashell.portforward.domain.model

/**
 * One saved tunnel: a port on this phone, and where its connections are relayed.
 *
 * @property shareOnNetwork listen on every interface instead of loopback only, so that other
 * devices on the same Wi-Fi network can use the tunnel. Honoured only while sharing is allowed in
 * settings.
 * @property autoStart start the tunnel when the app opens.
 * @property looksLikeHttp the target answered an HTTP request, or was entered as an http URL, so it
 * can be opened in a browser.
 */
data class TunnelSpec(
    val id: String,
    val name: String,
    val localPort: Int,
    val shareOnNetwork: Boolean,
    val target: TunnelTarget,
    val autoStart: Boolean,
    val looksLikeHttp: Boolean
)
