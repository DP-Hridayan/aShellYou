package `in`.hridayan.ashell.logcat.data.emitter

import `in`.hridayan.ashell.logcat.domain.emitter.LogcatEmitter
import `in`.hridayan.ashell.logcat.domain.permission.ReadLogsAccessChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.InterruptedIOException
import javax.inject.Inject

/**
 * Logcat emitter that runs `sh -c "logcat -v threadtime"` as a plain app subprocess.
 *
 * Used by the Log access mode; the process only sees the full system log when
 * READ_LOGS has been granted and this process holds the `log` group.
 */
class BasicLogcatEmitter @Inject constructor(
    private val accessChecker: ReadLogsAccessChecker,
) : LogcatEmitter {

    override fun lines(): Flow<String> = flow {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "logcat -v threadtime"))
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        try {
            while (true) {
                val line = reader.readLine() ?: break
                emit(line)
            }
        } catch (_: InterruptedIOException) {
        } catch (e: IOException) {
        } finally {
            try {
                reader.close()
            } catch (_: IOException) {
            }
            process.destroy()
        }
    }.flowOn(Dispatchers.IO)

    override fun isAvailable(): Boolean =
        accessChecker.isPermissionGranted() && accessChecker.hasLogGroup().getOrDefault(true)
}
