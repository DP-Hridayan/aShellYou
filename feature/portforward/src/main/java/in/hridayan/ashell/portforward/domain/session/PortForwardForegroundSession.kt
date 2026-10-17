package `in`.hridayan.ashell.portforward.domain.session

/**
 * Keeps the process running while any tunnel is running.
 *
 * Android freezes a cached process shortly after it leaves the screen, which would stall every
 * relayed connection, and reclaims cached processes first under memory pressure.
 */
interface PortForwardForegroundSession {
    fun start(): Result<Unit>
    fun stop()
}
