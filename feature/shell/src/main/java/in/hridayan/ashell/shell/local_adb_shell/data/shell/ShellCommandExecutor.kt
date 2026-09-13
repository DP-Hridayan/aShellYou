package `in`.hridayan.ashell.shell.local_adb_shell.data.shell

import android.content.Context
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuCommandRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.InterruptedIOException

private const val EMULATED_STORAGE_PREFIX = "/storage/emulated/"
private val SHIZUKU_ENVIRONMENT = arrayOf(
    "PATH=/product/bin:/apex/com.android.runtime/bin:/apex/com.android.art/bin:" +
        "/system_ext/bin:/system/bin:/system/xbin:/odm/bin:/vendor/bin"
)

class ShellCommandExecutor(
    private val shizukuCommandRunner: ShizukuCommandRunner
) {
    private var currentProcess: Process? = null
    private var currentDir = "/storage/emulated/0/"

    /**
     * Handle cd command and return the command to actually execute.
     * - If it's a pure "cd" command, updates currentDir and returns null.
     * - If it's a compound command starting with cd (e.g., "cd /data && ls"),
     *   updates currentDir and returns the remaining commands.
     * - Otherwise returns the original command.
     */
    private fun handleCdCommand(commandText: String): String? {
        val trimmedCommand = commandText.trim()

        // Check if this is a cd command (standalone or at start of compound)
        if (trimmedCommand.startsWith("cd ") || trimmedCommand == "cd") {
            // Check for compound command separators (&& or ;)
            val andAndIndex = trimmedCommand.indexOf(" && ")
            val semicolonIndex = trimmedCommand.indexOf("; ")

            val separatorIndex = when {
                andAndIndex >= 0 && semicolonIndex >= 0 -> minOf(andAndIndex, semicolonIndex)
                andAndIndex >= 0 -> andAndIndex
                semicolonIndex >= 0 -> semicolonIndex
                else -> -1
            }

            val cdPart: String
            val remainingCommand: String?

            if (separatorIndex > 0) {
                // Compound command: extract cd part and remaining
                cdPart = trimmedCommand.take(separatorIndex).trim()
                remainingCommand = trimmedCommand.substring(
                    separatorIndex + if (trimmedCommand.substring(separatorIndex)
                            .startsWith(" && ")
                    ) {
                        4
                    } else {
                        2
                    }
                ).trim()
            } else {
                // Pure cd command
                cdPart = trimmedCommand
                remainingCommand = null
            }

            // Parse the cd part to get target directory
            val parts = cdPart.split("\\s+".toRegex(), limit = 2)
            val targetDir = if (parts.size > 1) parts[1] else "/"

            // Update currentDir
            currentDir = when {
                targetDir == "/" || targetDir == "~" -> "/"
                targetDir == ".." -> {
                    val parent = currentDir.removeSuffix("/").substringBeforeLast("/", "")
                    if (parent.isEmpty()) "/" else "$parent/"
                }

                targetDir.startsWith("/") -> {
                    if (targetDir.endsWith("/")) targetDir else "$targetDir/"
                }

                else -> {
                    val newPath = currentDir + targetDir
                    if (newPath.endsWith("/")) newPath else "$newPath/"
                }
            }

            // Return remaining command or null if pure cd
            return remainingCommand
        }

        return trimmedCommand
    }

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
        val actualCommand = handleCdCommand(commandText)

        if (actualCommand == null) {
            // Was a cd command - emit success message
            emit(OutputLine("Changed directory to: $currentDir", isError = false))
            return@flow
        }

        val fullCommand = buildCommand(actualCommand)
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", fullCommand))
        emitAll(exec(process))
    }.flowOn(Dispatchers.IO)

    fun runRoot(commandText: String): Flow<OutputLine> = flow {
        val actualCommand = handleCdCommand(commandText)

        if (actualCommand == null) {
            // Was a cd command - emit success message
            emit(OutputLine("Changed directory to: $currentDir", isError = false))
            return@flow
        }

        val fullCommand = buildCommand(actualCommand)
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", fullCommand))
        emitAll(exec(process))
    }.flowOn(Dispatchers.IO)

    fun runShizuku(commandText: String): Flow<OutputLine> = flow {
        val actualCommand = handleCdCommand(commandText)

        if (actualCommand == null) {
            emit(OutputLine("Changed directory to: $currentDir", isError = false))
            return@flow
        }

        val process = shizukuCommandRunner
            .start(arrayOf("sh", "-c", actualCommand), SHIZUKU_ENVIRONMENT, shizukuWorkingDirectory())
            .getOrThrow()
        emitAll(exec(process))
    }.flowOn(Dispatchers.IO)

    private fun shizukuWorkingDirectory(): String? =
        currentDir.takeUnless { it.startsWith(EMULATED_STORAGE_PREFIX) }

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
