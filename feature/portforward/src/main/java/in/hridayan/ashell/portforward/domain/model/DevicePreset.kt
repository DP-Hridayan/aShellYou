package `in`.hridayan.ashell.portforward.domain.model

/**
 * Common device sockets, with the local port their tools expect.
 *
 * @property looksLikeHttp the socket answers HTTP, so its tunnel can be opened in a browser.
 */
enum class DevicePreset(val endpoint: DeviceEndpoint, val localPort: Int, val looksLikeHttp: Boolean) {
    CHROME_DEVTOOLS(
        endpoint = DeviceEndpoint(DeviceEndpointType.ABSTRACT, "chrome_devtools_remote"),
        localPort = 9222,
        looksLikeHttp = true
    ),
    FRIDA(
        endpoint = DeviceEndpoint(DeviceEndpointType.TCP, "27042"),
        localPort = 27042,
        looksLikeHttp = false
    )
}
