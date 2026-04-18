package `in`.hridayan.ashell.qstiles.data.executor

import android.content.pm.PackageManager
import `in`.hridayan.ashell.qstiles.domain.executor.CommandExecutor
import `in`.hridayan.ashell.qstiles.domain.executor.CommandResult
import `in`.hridayan.ashell.qstiles.domain.model.TileErrorType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import javax.inject.Inject

class ShizukuExecutor @Inject constructor() : CommandExecutor {

    @Suppress("DEPRECATION")
    override suspend fun execute(command: String): CommandResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()

        if (!Shizuku.pingBinder()) {
            return@withContext CommandResult(
                output = "Shizuku is not running.",
                isSuccess = false,
                errorType = TileErrorType.PERMISSION_DENIED,
                durationMs = elapsed(start)
            )
        }

        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            return@withContext CommandResult(
                output = "Shizuku permission not granted.",
                isSuccess = false,
                errorType = TileErrorType.PERMISSION_DENIED,
                durationMs = elapsed(start)
            )
        }

        var process: Process? = null
        try {
            process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)

            val output = StringBuilder()
            val errorOutput = StringBuilder()

            val stdoutThread = Thread {
                process.inputStream.bufferedReader().forEachLine { output.appendLine(it) }
            }.apply { start() }

            val stderrThread = Thread {
                process.errorStream.bufferedReader().forEachLine { errorOutput.appendLine(it) }
            }.apply { start() }

            val exitCode = process.waitFor()
            stdoutThread.join()
            stderrThread.join()

            val combinedOutput = buildString {
                if (output.isNotBlank()) append(output.trim())
                if (errorOutput.isNotBlank()) {
                    if (isNotEmpty()) appendLine()
                    append(errorOutput.trim())
                }
            }

            CommandResult(
                output = combinedOutput.ifBlank { "Command executed (no output)." },
                isSuccess = exitCode == 0,
                errorType = if (exitCode == 0) TileErrorType.NONE else TileErrorType.EXECUTION_FAILED,
                durationMs = elapsed(start)
            )
        } catch (e: CancellationException) {
            process?.destroy()
            throw e
        } catch (e: Exception) {
            process?.destroy()
            CommandResult(
                output = "Execution error: ${e.message}",
                isSuccess = false,
                errorType = TileErrorType.EXECUTION_FAILED,
                durationMs = elapsed(start)
            )
        }
    }

    private fun elapsed(start: Long) = System.currentTimeMillis() - start
}
