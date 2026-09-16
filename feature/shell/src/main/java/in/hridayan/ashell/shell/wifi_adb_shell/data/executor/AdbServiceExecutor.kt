package `in`.hridayan.ashell.shell.wifi_adb_shell.data.executor

import android.content.Context
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.common.data.adb.AdbConnectionManager
import io.github.muntashirakon.adb.AdbStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.nio.charset.StandardCharsets

private const val READ_BUFFER_SIZE = 4096
private const val RESTART_REPLY_PREFIX = "restarting"

/**
 * Runs an ADB service that the daemon implements natively, such as `tcpip:5555` or `reboot:`.
 *
 * Unlike the interactive shell service these replies are short and the daemon closes the stream when
 * it is done, so the reader runs to end of stream instead of guessing from an idle timeout.
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

        val reply = readReply(stream)
        reply.forEach { emit(OutputLine(it, isError = false)) }

        if (!restartsDaemon || !reply.confirmsRestart()) return@flow

        emit(infoLine(R.string.adb_daemon_restarted))
        reconnectCommand?.let {
            emit(OutputLine(context.getString(R.string.adb_reconnect_hint, it), isError = false))
        }
        onDaemonRestarted()
    }

    private fun readReply(stream: AdbStream): List<String> {
        val buffer = StringBuilder()
        stream.appendTextTo(buffer)
        return buffer.toString()
            .split('\n')
            .map { it.trimEnd('\r') }
            .filter { it.isNotBlank() }
    }

    /**
     * Reads to end of stream, keeping whatever arrived before a failure.
     *
     * A restarting service such as `tcpip:` answers and then drops the whole connection, which
     * surfaces as an [IOException] on the following read rather than a clean end of stream. The
     * reply already in [buffer] is still the daemon's real answer, so it is kept.
     */
    private fun AdbStream.appendTextTo(buffer: StringBuilder) {
        val raw = ByteArray(READ_BUFFER_SIZE)
        try {
            openInputStream().use { input ->
                var bytesRead = input.read(raw, 0, raw.size)
                while (bytesRead >= 0) {
                    buffer.append(String(raw, 0, bytesRead, StandardCharsets.UTF_8))
                    bytesRead = input.read(raw, 0, raw.size)
                }
            }
        } catch (_: IOException) {
            return
        }
    }

    private fun List<String>.confirmsRestart(): Boolean =
        isEmpty() || first().trimStart().startsWith(RESTART_REPLY_PREFIX, ignoreCase = true)

    private fun errorLine(resId: Int) = OutputLine(context.getString(resId), isError = true)

    private fun infoLine(resId: Int) = OutputLine(context.getString(resId), isError = false)

    private fun failureLine(error: Exception) = OutputLine(
        context.getString(R.string.command_execution_failed, error.message),
        isError = true
    )
}
