package `in`.hridayan.ashell.mirror.data.server

import android.util.Log
import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.mirror.domain.protocol.LineSplitter
import `in`.hridayan.ashell.mirror.domain.protocol.ServerCommandBuilder
import `in`.hridayan.ashell.mirror.domain.protocol.ServerLogClassifier
import `in`.hridayan.ashell.mirror.domain.protocol.ServerLogSignal
import java.io.IOException

private const val LOG_HISTORY_LINES = 50
private const val ERROR_MARKER = "ERROR:"
private const val TAG = "MirrorServer"

/**
 * The running server, reached through the `shell:` stream that launched it.
 *
 * The stream stays open for the whole session: its output is the server's log, and its closing is
 * how the session learns the server exited. Closing it from this side ends the server.
 */
class ServerProcess(private val stream: AdbDuplexStream) {

    private val history = ArrayDeque<String>()

    /** The server's pid on the device, once the launch command has announced it. */
    @Volatile
    var pid: Int? = null
        private set

    /**
     * Reads the server's output until it exits, reporting each line that changes what the session
     * should do. Returns when the stream closes.
     */
    suspend fun pumpLogs(onSignal: (ServerLogSignal) -> Unit) {
        val splitter = LineSplitter()
        try {
            while (true) {
                splitter.feed(stream.read()).forEach { line -> handleLine(line, onSignal) }
            }
        } catch (_: IOException) {
        }
    }

    private fun handleLine(line: String, onSignal: (ServerLogSignal) -> Unit) {
        ServerCommandBuilder.parseServerPid(line)?.let { pid = it }
        remember(line)
        ServerLogClassifier.classify(line)?.let(onSignal)
    }

    /**
     * Up to [count] lines that best explain a failure: from the most recent error line onward, since
     * the tail of a log is usually just stack frames and the shell's own epilogue.
     */
    fun diagnosticLines(count: Int): List<String> = synchronized(history) {
        val lastError = history.indexOfLast { ERROR_MARKER in it }
        if (lastError < 0) history.takeLast(count) else history.drop(lastError).take(count)
    }

    fun stop() = stream.close()

    private fun remember(line: String) = synchronized(history) {
        Log.d(TAG, line)
        history.addLast(line)
        if (history.size > LOG_HISTORY_LINES) history.removeFirst()
    }
}
