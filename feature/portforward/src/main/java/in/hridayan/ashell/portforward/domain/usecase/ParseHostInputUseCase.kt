package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.portforward.domain.model.ParsedHostInput
import java.net.URI
import java.net.URISyntaxException
import javax.inject.Inject

/**
 * Splits a pasted URL or a typed `host[:port][/path]` into its parts.
 *
 * Returns null when the input cannot name a host, or names a port outside 1–65535. A URL with a
 * known scheme and no port gets that scheme's default port.
 */
class ParseHostInputUseCase @Inject constructor() {

    operator fun invoke(input: String): ParsedHostInput? {
        val trimmed = input.trim()
        if (trimmed.isEmpty() || trimmed.any { it.isWhitespace() }) return null
        return if (trimmed.contains(SCHEME_SEPARATOR)) parseUrl(trimmed) else parseAddress(trimmed)
    }

    private fun parseUrl(input: String): ParsedHostInput? {
        val uri = toUriOrNull(input) ?: return null
        val host = uri.host?.removeSurrounding(IPV6_OPEN, IPV6_CLOSE)?.takeIf { it.isNotEmpty() } ?: return null
        val scheme = uri.scheme?.lowercase()
        if (uri.port != NO_PORT && !uri.port.isValidPort()) return null
        val port = if (uri.port == NO_PORT) DEFAULT_PORTS[scheme] else uri.port
        return ParsedHostInput(
            host = host,
            port = port,
            path = pathOf(uri.rawPath, uri.rawQuery),
            isHttp = scheme in HTTP_SCHEMES
        )
    }

    private fun toUriOrNull(input: String): URI? = try {
        URI(input)
    } catch (_: URISyntaxException) {
        null
    }

    private fun parseAddress(input: String): ParsedHostInput? {
        val slash = input.indexOf(PATH_SEPARATOR)
        val authority = if (slash >= 0) input.substring(0, slash) else input
        val path = if (slash >= 0) input.substring(slash).takeIf { it != PATH_SEPARATOR.toString() } else null
        val (host, portText) = splitAuthority(authority) ?: return null
        val port = portText?.let { text -> text.toIntOrNull()?.takeIf { it.isValidPort() } ?: return null }
        return ParsedHostInput(host = host, port = port, path = path, isHttp = false)
    }

    private fun splitAuthority(authority: String): Pair<String, String?>? = when {
        authority.startsWith(IPV6_OPEN) -> splitBracketedIpv6(authority)
        authority.count { it == PORT_SEPARATOR } > 1 -> authority to null
        authority.contains(PORT_SEPARATOR) -> authority.substringBefore(PORT_SEPARATOR) to
            authority.substringAfter(PORT_SEPARATOR)
        else -> authority to null
    }?.takeIf { it.first.isNotEmpty() }

    private fun splitBracketedIpv6(authority: String): Pair<String, String?>? {
        val close = authority.indexOf(IPV6_CLOSE)
        if (close < 0) return null
        val host = authority.substring(1, close)
        val rest = authority.substring(close + 1)
        val port = if (rest.startsWith(PORT_SEPARATOR)) rest.substring(1) else null
        return host to port
    }

    private fun pathOf(rawPath: String?, rawQuery: String?): String? {
        val path = rawPath?.takeIf { it.isNotEmpty() && it != PATH_SEPARATOR.toString() }
        val query = rawQuery?.takeIf { it.isNotEmpty() } ?: return path
        return (path ?: PATH_SEPARATOR.toString()) + QUERY_SEPARATOR + query
    }

    private fun Int.isValidPort(): Boolean = this in MIN_PORT..MAX_PORT

    private companion object {
        const val SCHEME_SEPARATOR = "://"
        const val PATH_SEPARATOR = '/'
        const val PORT_SEPARATOR = ':'
        const val QUERY_SEPARATOR = '?'
        const val IPV6_OPEN = "["
        const val IPV6_CLOSE = "]"
        const val NO_PORT = -1
        const val MIN_PORT = 1
        const val MAX_PORT = 65535
        const val HTTP_DEFAULT_PORT = 80
        const val HTTPS_DEFAULT_PORT = 443

        val HTTP_SCHEMES = setOf("http", "https")
        val DEFAULT_PORTS = mapOf(
            "http" to HTTP_DEFAULT_PORT,
            "https" to HTTPS_DEFAULT_PORT,
            "ws" to HTTP_DEFAULT_PORT,
            "wss" to HTTPS_DEFAULT_PORT
        )
    }
}
