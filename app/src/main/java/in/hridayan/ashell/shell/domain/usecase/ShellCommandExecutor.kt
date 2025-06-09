package `in`.hridayan.ashell.shell.domain.usecase

import android.content.pm.PackageManager
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.Shell.FLAG_MOUNT_MASTER
import `in`.hridayan.ashell.core.common.constants.LocalAdbWorkingMode
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var rootShell: Shell? = null
    private var rootJob: Shell.Job? = null
    private var shizukuShell: ShizukuRemoteProcess? = null
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
        else -> flow {}
    }.flowOn(Dispatchers.IO)

    fun runBasic(commandText: String): Flow<OutputLine> {
        return exec(Runtime.getRuntime().exec(arrayOf("sh", "-c", commandText)))
    }

    fun runRoot(commandText: String): Flow<OutputLine> {
        return exec(Runtime.getRuntime().exec(arrayOf("su", "-c", commandText)))
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
            } catch (_: IOException) {}

            currentProcess?.destroy()
            currentProcess = null
        }
    }

 /*  private suspend fun runRoot(
        commandText: String,
        outputFlow: MutableStateFlow<List<OutputLine>>
    ) {
        withContext(Dispatchers.IO) {
            try {
                if (rootShell == null || rootShell?.isAlive != true) {
                    rootShell = Shell.Builder.create().apply {
                        setFlags(FLAG_MOUNT_MASTER)
                    }.build()
                }

                if (rootShell?.isRoot == false) {
                    return@withContext
                }

                runGenericProcess(
                    Runtime.getRuntime().exec(arrayOf("su", "-c", commandText)), outputFlow
                )

            } catch (e: Exception) {
                outputFlow.update { it + OutputLine("Root shell error: ${e.message}", true) }
            }
        }
    }
*/
    @Suppress("DEPRECATION")
    private suspend fun runShizuku(
        commandText: String,
        outputFlow: MutableStateFlow<List<OutputLine>>
    ) {
        withContext(Dispatchers.IO) {
            try {
                runGenericProcess(
                    Shizuku.newProcess(
                        arrayOf("sh", "-c", commandText),
                        null,
                        currentDir
                    ), outputFlow
                )

                if (commandText.startsWith("cd ") && !outputFlow.value.last().isError) {
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

            } catch (e: Exception) {
                outputFlow.update { it + OutputLine("Shizuku error: ${e.message}", true) }
            }
        }
    }

    private fun hasShizukuPermission(): Boolean {
        return Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
    }

    suspend fun runGenericProcess(
        process: Process,
        outputFlow: MutableStateFlow<List<OutputLine>>
    ) = withContext(Dispatchers.IO) {
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))

        try {
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let { line ->
                    outputFlow.update { it + OutputLine(line, false) }
                }
            }

            while (errorReader.readLine().also { line = it } != null) {
                line?.let { line ->
                    outputFlow.update { it + OutputLine(line, true) }
                }
            }

            process.waitFor()
        } catch (e: Exception) {
            outputFlow.update { it + OutputLine("Error executing command: ${e.message}", true) }
        }
    }


    fun stop() {
        currentProcess?.destroy()
        currentProcess = null
        rootShell?.close()
        rootShell = null
    }

    companion object {
        const val MODE_BASIC = LocalAdbWorkingMode.BASIC
        const val MODE_ROOT = LocalAdbWorkingMode.ROOT
        const val MODE_SHIZUKU = LocalAdbWorkingMode.SHIZUKU
        const val DEFAULT_MODE = MODE_BASIC
    }
}