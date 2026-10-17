package `in`.hridayan.ashell.core.common.domain.model

/**
 * Selectable limits for port-forwarding tunnels.
 *
 * [sanitizeConnectTimeout] and [sanitizeMaxConnections] keep values from a restored backup or an
 * older install inside the offered options.
 */
object PortForwardDefaults {
    const val CONNECT_TIMEOUT_SECONDS = 5
    const val MAX_CONNECTIONS = 16
    const val WAIT_TIMEOUT_MINUTES = 5
    const val WAIT_FOREVER = -1

    private const val MILLIS_PER_MINUTE = 60_000L

    val CONNECT_TIMEOUT_OPTIONS: List<Int> = listOf(3, 5, 10, 30)
    val MAX_CONNECTIONS_OPTIONS: List<Int> = listOf(8, 16, 32, 64)
    val WAIT_TIMEOUT_OPTIONS: List<Int> = listOf(1, 5, 15, 30, WAIT_FOREVER)

    fun sanitizeConnectTimeout(seconds: Int): Int =
        if (seconds in CONNECT_TIMEOUT_OPTIONS) seconds else CONNECT_TIMEOUT_SECONDS

    fun sanitizeMaxConnections(count: Int): Int =
        if (count in MAX_CONNECTIONS_OPTIONS) count else MAX_CONNECTIONS

    fun sanitizeWaitTimeout(minutes: Int): Int =
        if (minutes in WAIT_TIMEOUT_OPTIONS) minutes else WAIT_TIMEOUT_MINUTES

    /** The wait in milliseconds, or null for [WAIT_FOREVER]. */
    fun waitTimeoutMillis(minutes: Int): Long? =
        sanitizeWaitTimeout(minutes).takeIf { it != WAIT_FOREVER }?.let { it * MILLIS_PER_MINUTE }
}
