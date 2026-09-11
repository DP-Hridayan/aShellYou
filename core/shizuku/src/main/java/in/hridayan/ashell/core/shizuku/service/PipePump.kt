package `in`.hridayan.ashell.core.shizuku.service

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private const val BUFFER_SIZE = 8192
private const val THREAD_NAME = "ShizukuPipePump"

/**
 * Copies [source] into [sink] on a background thread and closes both ends when the copy ends.
 * Used to bridge a child process stream to a pipe that can cross the binder boundary.
 */
class PipePump(
    private val source: InputStream,
    private val sink: OutputStream
) : Thread(THREAD_NAME) {

    init {
        isDaemon = true
    }

    override fun run() {
        try {
            copy()
        } catch (_: IOException) {
        } finally {
            closeQuietly()
        }
    }

    private fun copy() {
        val buffer = ByteArray(BUFFER_SIZE)
        while (true) {
            val read = source.read(buffer)
            if (read < 0) break
            sink.write(buffer, 0, read)
            sink.flush()
        }
    }

    private fun closeQuietly() {
        try {
            source.close()
        } catch (_: IOException) {
        }
        try {
            sink.close()
        } catch (_: IOException) {
        }
    }
}
