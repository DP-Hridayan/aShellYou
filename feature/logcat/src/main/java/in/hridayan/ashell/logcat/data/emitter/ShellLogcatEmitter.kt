package `in`.hridayan.ashell.logcat.data.emitter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll

import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.InterruptedIOException
import javax.inject.Inject

/**
 * Logcat emitter that runs `sh -c "logcat -v threadtime"` as a subprocess.
 *
 * This is the default emitter for basic shell mode. Future emitters
 * (Shizuku, Wireless ADB, OTG) implement [LogcatEmitter] and are
 * swapped in via Hilt bindings without touching this class.
 */
class ShellLogcatEmitter @Inject constructor() : LogcatEmitter {

    override fun lines(): Flow<String> = flow {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "logcat -v threadtime"))
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        try {
            while (true) {
                val line = reader.readLine() ?: break
                emit(line)
            }
        } catch (_: InterruptedIOException) {
            // Cancelled normally
        } catch (e: IOException) {
            // Process destroyed externally — treat as normal termination
        } finally {
            try { reader.close() } catch (_: IOException) {}
            process.destroy()
        }
    }.flowOn(Dispatchers.IO)

    override fun isAvailable(): Boolean = true
}
