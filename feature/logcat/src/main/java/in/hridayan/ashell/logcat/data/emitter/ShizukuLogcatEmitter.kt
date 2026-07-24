package `in`.hridayan.ashell.logcat.data.emitter

import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.InterruptedIOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Logcat emitter that runs logcat as the `shell` UID via Shizuku.
 * Does not require the READ_LOGS permission — Shizuku grants full access.
 */
@Singleton
class ShizukuLogcatEmitter @Inject constructor() : LogcatEmitter {

    override fun lines(): Flow<String> = flow {
        if (!runCatching { Shizuku.pingBinder() }.getOrDefault(false)) return@flow
        val process = Shizuku.newProcess(
            arrayOf("logcat", "-v", "threadtime"),
            null,
            null
        )
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        try {
            while (true) {
                val line = reader.readLine() ?: break
                emit(line)
            }
        } catch (_: InterruptedIOException) {
        } catch (_: IOException) {
        } finally {
            try { reader.close() } catch (_: IOException) {}
            process.destroy()
        }
    }.flowOn(Dispatchers.IO)

    override fun isAvailable(): Boolean =
        runCatching { Shizuku.pingBinder() }.getOrDefault(false)
}
