package `in`.hridayan.ashell.shell.local_adb_shell.data.shell

import android.content.Context
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuCommandRunner
import `in`.hridayan.ashell.shell.common.domain.shell.DirectoryResult
import `in`.hridayan.ashell.shell.common.domain.shell.ShellDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.InterruptedIOException

private val SHIZUKU_ENVIRONMENT = arrayOf(
    "PATH=/product/bin:/apex/com.android.runtime/bin:/apex/com.android.art/bin:" +
        "/system_ext/bin:/system/bin:/system/xbin:/odm/bin:/vendor/bin"
)

class ShellCommandExecutor(
    private val context: Context,
    private val shizukuCommandRunner: ShizukuCommandRunner
) {
    private var currentProcess: Process? = null
    private var currentDir = "/storage/emulated/0/"

    /**
     * Build the actual command to run, prefixed with cd if not in root.
     */
    private fun buildCommand(commandText: String): String {
        return if (currentDir != "/") {
            "cd '$currentDir' && $commandText"
        } else {
            commandText
        }
    }

    fun runBasic(commandText: String, context: Context): Flow<OutputLine> = flow {
        val actualCommand =
            applyDirectoryChange(commandText, ::startBasic) { emit(it) } ?: return@flow

        emitAll(exec(startBasic(buildCommand(actualCommand))))
    }.flowOn(Dispatchers.IO)

    fun runRoot(commandText: String): Flow<OutputLine> = flow {
        val actualCommand =
            applyDirectoryChange(commandText, ::startRoot) { emit(it) } ?: return@flow

        emitAll(exec(startRoot(buildCommand(actualCommand))))
    }.flowOn(Dispatchers.IO)

    fun runShizuku(commandText: String): Flow<OutputLine> = flow {
        val actualCommand =
            applyDirectoryChange(commandText, ::startShizuku) { emit(it) } ?: return@flow

        emitAll(exec(startShizuku(buildCommand(actualCommand))))
    }.flowOn(Dispatchers.IO)

    private fun startBasic(command: String): Process =
        Runtime.getRuntime().exec(arrayOf("sh", "-c", command))

    private fun startRoot(command: String): Process =
        Runtime.getRuntime().exec(arrayOf("su", "-c", command))

    /**
     * Shizuku used to receive the directory as the helper's working directory, and skipped it
     * entirely under emulated storage, so `cd` there was silently ignored. Prefixing the command is
     * what the other two modes already do, and it works everywhere.
     */
    private suspend fun startShizuku(command: String): Process = shizukuCommandRunner
        .start(arrayOf("sh", "-c", command), SHIZUKU_ENVIRONMENT, null)
        .getOrThrow()

    /**
     * Applies a `cd` by asking the shell, and returns what is left to run.
     *
     * The probe runs through the same starter as the command itself, so a root-only directory is
     * judged with root privileges rather than the app's.
     *
     * @return the command to execute, or null when nothing remains, either because the input was
     * only a `cd` or because the directory was refused.
     */
    private suspend fun applyDirectoryChange(
        commandText: String,
        start: suspend (String) -> Process,
        emit: suspend (OutputLine) -> Unit
    ): String? {
        val target = ShellDirectory.targetOf(commandText) ?: return commandText

        val candidate = ShellDirectory.candidate(currentDir, target)
        val output = probeOnce(ShellDirectory.probeCommand(candidate), start)

        return when (val result = ShellDirectory.interpret(output)) {
            is DirectoryResult.Moved -> {
                currentDir = result.path
                emit(OutputLine(context.getString(R.string.changed_directory_to, result.path)))
                ShellDirectory.remainderOf(commandText)
            }

            is DirectoryResult.Rejected -> {
                val reason = result.reason
                    ?: context.getString(R.string.no_such_directory, candidate)
                emit(OutputLine(reason, isError = true))
                null
            }
        }
    }

    private suspend fun probeOnce(command: String, start: suspend (String) -> Process): String? = try {
        val process = start(command)
        val output = process.inputStream.bufferedReader().readText() +
            process.errorStream.bufferedReader().readText()
        process.waitFor()
        output
    } catch (e: Exception) {
        null
    }

    suspend fun warmUp() {
        shizukuCommandRunner.warmUp()
    }

    fun exec(process: Process): Flow<OutputLine> = flow {
        currentProcess = process
        val reader = BufferedReader(InputStreamReader(currentProcess?.inputStream))
        val errorReader = BufferedReader(InputStreamReader(currentProcess?.errorStream))

        try {
            while (true) {
                val line = reader.readLine() ?: break
                emit(OutputLine(line, isError = false))
            }

            while (true) {
                val errorLine = errorReader.readLine() ?: break
                emit(OutputLine(errorLine, isError = true))
            }

            currentProcess?.waitFor()
        } catch (e: InterruptedIOException) {
        } catch (e: IOException) {
            emit(OutputLine("Error reading process output: ${e.message}", isError = true))
        } finally {
            try {
                reader.close()
                errorReader.close()
            } catch (_: IOException) {
            }

            currentProcess?.destroy()
            currentProcess = null
        }
    }.flowOn(Dispatchers.IO)

    fun stop() {
        currentProcess?.destroy()
        currentProcess = null
    }
}
