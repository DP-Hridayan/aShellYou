package `in`.hridayan.ashell.shell.domain.usecase

import `in`.hridayan.ashell.core.common.constants.LocalAdbWorkingMode
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.InterruptedIOException

class ShellCommandExecutor(
    private val settingsRepository: SettingsRepository
) {
    private var currentProcess: Process? = null
    private var currentAdbMode: Int = DEFAULT_MODE
    private var shizukuProcess: ShizukuRemoteProcess? = null
    private var currentDir = "/"

    init {
        CoroutineScope(Dispatchers.IO).launch {
            settingsRepository
                .getInt(SettingsKeys.LOCAL_ADB_WORKING_MODE)
                .collect { mode ->
                    currentAdbMode = mode
                }
        }
    }

    fun executeCommand(commandText: String): Flow<OutputLine> = when (currentAdbMode) {
        MODE_BASIC -> runBasic(commandText)
        MODE_ROOT -> runRoot(commandText)
        MODE_SHIZUKU -> runShizuku(commandText)
        else -> flow { }
    }.flowOn(Dispatchers.IO)

    fun runBasic(commandText: String): Flow<OutputLine> {
        return exec(Runtime.getRuntime().exec(arrayOf("sh", "-c", commandText)))
    }

    fun runRoot(commandText: String): Flow<OutputLine> {
        return exec(Runtime.getRuntime().exec(arrayOf("su", "-c", commandText)))
    }

    @Suppress("DEPRECATION")
    fun runShizuku(commandText: String): Flow<OutputLine> = flow {
        if (commandText.startsWith("cd ")) {
            val parts = commandText.trim().split("\\s+".toRegex())
            var dir = parts.last()
            dir = when {
                dir == "/" -> "/"
                dir.startsWith("/") -> dir
                else -> currentDir + dir
            }
            if (!dir.endsWith("/")) dir += "/"
            currentDir = dir
        }

        shizukuProcess = Shizuku.newProcess(
            arrayOf("sh", "-c", commandText),
            null,
            currentDir
        )

        val reader = BufferedReader(InputStreamReader(shizukuProcess?.inputStream))
        val errorReader = BufferedReader(InputStreamReader(shizukuProcess?.errorStream))

        try {
            while (true) {
                val line = reader.readLine() ?: break
                emit(OutputLine(line, isError = false))
            }

            while (true) {
                val errorLine = errorReader.readLine() ?: break
                emit(OutputLine(errorLine, isError = true))
            }

            shizukuProcess?.waitFor()
        } catch (e: InterruptedIOException) {
        } catch (e: IOException) {
            emit(OutputLine("Error reading process output: ${e.message}", isError = true))
        } finally {
            try {
                reader.close()
                errorReader.close()
            } catch (_: IOException) {
            }

            shizukuProcess?.destroy()
            shizukuProcess = null
        }
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
    }

    fun stop() {
        currentProcess?.destroy()
        currentProcess = null
        shizukuProcess?.destroy()
        shizukuProcess = null
    }

    companion object {
        const val MODE_BASIC = LocalAdbWorkingMode.BASIC
        const val MODE_ROOT = LocalAdbWorkingMode.ROOT
        const val MODE_SHIZUKU = LocalAdbWorkingMode.SHIZUKU
        const val DEFAULT_MODE = MODE_BASIC
    }
}