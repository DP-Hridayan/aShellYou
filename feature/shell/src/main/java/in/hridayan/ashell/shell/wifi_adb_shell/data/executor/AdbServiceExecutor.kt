package `in`.hridayan.ashell.shell.wifi_adb_shell.data.executor

import android.content.Context
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import io.github.muntashirakon.adb.AdbStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.io.IOException

private const val RESTART_REPLY_PREFIX = "restarting"
private const val POLL_INTERVAL_MS = 20L
private const val IDLE_TIMEOUT_MS = 500L

/**
 * A restarting service answers at once or not at all, so waiting long adds nothing.
 */
private const val RESTART_FIRST_REPLY_TIMEOUT_MS = 3_000L

/**
 * Other services, such as `remount:` and `exec:`, can take a moment to produce their first output.
 */
private const val DEFAULT_FIRST_REPLY_TIMEOUT_MS = 15_000L

/**
 * Runs an ADB service that the daemon implements natively, such as `tcpip:5555` or `reboot:`.
 *
 * Output is emitted line by line as it arrives. Holding it until the stream ended meant a reply that
 * had already been received was not shown, and a service that answers with nothing, such as
 * `reboot:`, showed nothing at all.
 */
class AdbServiceExecutor(private val context: Context) {

    /**
     * @param onStreamOpened publishes the stream so an abort request can close it.
     * @param onDaemonRestarted invoked when the daemon confirmed it is restarting, which also means
     * the current connection is gone.
     */
    fun execute(
        service: String,
        restartsDaemon: Boolean,
        reconnectCommand: String? = null,
        onStreamOpened: (AdbStream) -> Unit,
        onDaemonRestarted: () -> Unit
    ): Flow<OutputLine> = flow {
        val manager = runCatching { AdbConnectionManager.getInstance(context) }.getOrNull()
        if (manager == null || !manager.isConnected) {
            emit(errorLine(R.string.adb_not_connected))
            return@flow
        }

        val stream = try {
            manager.openStream(service)
        } catch (e: Exception) {
            emit(failureLine(e))
            return@flow
        }
        onStreamOpened(stream)

        val firstLine = emitReply(stream, restartsDaemon)
        if (!restartsDaemon || !confirmsRestart(firstLine)) return@flow

        emitRestartNotice(reconnectCommand)
        onDaemonRestarted()
    }

    /**
     * @return the first line of the reply, or null when the service answered with nothing.
     */
    private suspend fun FlowCollector<OutputLine>.emitReply(
        stream: AdbStream,
        restartsDaemon: Boolean
    ): String? {
        var firstLine: String? = null
        try {
            readerFor(restartsDaemon)
                .lines(stream.openInputStream()) { stream.isClosed }
                .forEach { line ->
                    if (firstLine == null) firstLine = line
                    emit(OutputLine(line, isError = false))
                }
        } finally {
            stream.closeQuietly()
        }
        return firstLine
    }

    private suspend fun FlowCollector<OutputLine>.emitRestartNotice(reconnectCommand: String?) {
        emit(infoLine(R.string.adb_daemon_restarted))
        reconnectCommand?.let {
            emit(OutputLine(context.getString(R.string.adb_reconnect_hint, it), isError = false))
        }
    }

    private fun readerFor(restartsDaemon: Boolean) = AdbReplyReader(
        ReplyTimeouts(
            firstReplyMs = if (restartsDaemon) {
                RESTART_FIRST_REPLY_TIMEOUT_MS
            } else {
                DEFAULT_FIRST_REPLY_TIMEOUT_MS
            },
            idleMs = IDLE_TIMEOUT_MS,
            pollIntervalMs = POLL_INTERVAL_MS
        )
    )

    /**
     * `reboot:` answers with nothing at all, so an empty reply still means the daemon is going away.
     * `root:` and `unroot:` answer either way, and only the restarting form drops the connection.
     */
    private fun confirmsRestart(firstLine: String?): Boolean =
        firstLine == null || firstLine.trimStart().startsWith(RESTART_REPLY_PREFIX, true)

    private fun errorLine(resId: Int) = OutputLine(context.getString(resId), isError = true)

    private fun infoLine(resId: Int) = OutputLine(context.getString(resId), isError = false)

    private fun failureLine(error: Exception) = OutputLine(
        context.getString(R.string.command_execution_failed, error.message),
        isError = true
    )
}

private fun AdbStream.closeQuietly() {
    try {
        close()
    } catch (_: IOException) {
        // A restarting daemon takes the connection with it, so the goodbye packet cannot be sent.
    }
}
