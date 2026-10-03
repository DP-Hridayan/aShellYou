package `in`.hridayan.ashell.mirror.data.server

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannel
import `in`.hridayan.ashell.mirror.domain.protocol.LineSplitter
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

private const val SHELL_SERVICE = "shell:"

/**
 * Runs a short, best-effort shell command on the device and waits for it to finish, which on this
 * transport means the stream closing. Failures are ignored: callers use it for cleanup that is worth
 * trying but never worth failing a session over. [onLine] receives the command's output.
 */
internal suspend fun ExternalDeviceChannel.runQuietly(
    command: String,
    timeoutMs: Long,
    onLine: (String) -> Unit = {}
) {
    val stream = openStream(SHELL_SERVICE + command).getOrNull() ?: return
    val splitter = LineSplitter()
    try {
        withTimeoutOrNull(timeoutMs) {
            while (true) splitter.feed(stream.read()).forEach(onLine)
        }
    } catch (_: IOException) {
    } finally {
        stream.close()
    }
}

/** Polls until the transport reports a connection again, for a bounded number of tries. */
internal suspend fun ExternalDeviceChannel.awaitReconnect(polls: Int, intervalMs: Long): Boolean {
    repeat(polls) {
        delay(intervalMs)
        if (isConnected) return true
    }
    return false
}
