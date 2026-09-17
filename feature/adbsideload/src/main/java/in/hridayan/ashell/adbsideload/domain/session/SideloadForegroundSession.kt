package `in`.hridayan.ashell.adbsideload.domain.session

/**
 * Keeps the process running while a package is streaming.
 *
 * Android freezes the threads of a cached process ten seconds after it stops being visible, which
 * would suspend the transfer, and a cached process is also the first thing reclaimed under memory
 * pressure. A foreground service keeps the process out of that state for the duration.
 */
interface SideloadForegroundSession {
    fun start()
    fun stop()
}
