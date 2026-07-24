package `in`.hridayan.ashell.logcat.data.emitter

import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.InterruptedIOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Logcat emitter that runs logcat as root via `su -c logcat`.
 * Does not require the READ_LOGS permission — root grants full access.
 */
@Singleton
class RootLogcatEmitter @Inject constructor() : LogcatEmitter {

    override fun lines(): Flow<String> = flow {
        val process = Runtime.getRuntime()
            .exec(arrayOf("su", "-c", "logcat -v threadtime"))
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

    override fun isAvailable(): Boolean = runCatching {
        Runtime.getRuntime().exec(arrayOf("su", "-c", "id")).waitFor() == 0
    }.getOrDefault(false)
}
