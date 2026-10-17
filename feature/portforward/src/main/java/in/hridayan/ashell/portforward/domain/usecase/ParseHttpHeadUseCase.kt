package `in`.hridayan.ashell.portforward.domain.usecase

import javax.inject.Inject

/** Reads the status code and `Server` header out of the head of an HTTP reply. */
class ParseHttpHeadUseCase @Inject constructor() {

    data class HttpHead(val status: Int, val server: String?)

    /** @param lines the reply up to, not including, the blank line that ends its head. */
    operator fun invoke(lines: List<String>): HttpHead? {
        val status = lines.firstOrNull()?.let(::statusOf) ?: return null
        return HttpHead(status = status, server = headerValue(lines.drop(1), SERVER_HEADER))
    }

    private fun statusOf(statusLine: String): Int? {
        val parts = statusLine.split(' ')
        if (parts.size < MIN_STATUS_LINE_PARTS || !parts[0].startsWith(HTTP_VERSION_PREFIX)) return null
        return parts[1].toIntOrNull()
    }

    private fun headerValue(headers: List<String>, name: String): String? = headers
        .firstOrNull { it.substringBefore(HEADER_SEPARATOR).trim().equals(name, ignoreCase = true) }
        ?.substringAfter(HEADER_SEPARATOR)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    private companion object {
        const val HTTP_VERSION_PREFIX = "HTTP/"
        const val SERVER_HEADER = "Server"
        const val HEADER_SEPARATOR = ':'
        const val MIN_STATUS_LINE_PARTS = 2
    }
}
