package `in`.hridayan.ashell.shell.domain.usecase

import android.content.pm.PackageManager
import com.topjohnwu.superuser.Shell
import `in`.hridayan.ashell.core.common.constants.LocalAdbWorkingMode
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.InterruptedIOException

class ShellCommandExecutor(
    private val settingsRepository: SettingsRepository
) {
    private var currentProcess: Process? = null
    private var currentAdbMode: Int = DEFAULT_MODE
    private var rootShell: Shell? = null
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

    suspend fun executeCommand(
        commandText: String,
        outputFlow: MutableStateFlow<List<OutputLine>>
    ) {
        withContext(Dispatchers.IO) {
            try {
                when (currentAdbMode) {
                    MODE_BASIC -> runBasic(commandText, outputFlow)
                    MODE_ROOT -> runRoot(commandText, outputFlow)
                    MODE_SHIZUKU -> runShizuku(commandText, outputFlow)
                    else -> {
                        outputFlow.update { it + OutputLine("Unknown ADB mode", true) }
                    }
                }
            } catch (e: Exception) {
                outputFlow.update { it + OutputLine("Executor error: ${e.message}", true) }
            }
        }
    }

    private suspend fun runBasic(
        commandText: String,
        outputFlow: MutableStateFlow<List<OutputLine>>
    ) {
        runGenericProcess(Runtime.getRuntime().exec(arrayOf("sh", "-c", commandText)), outputFlow)
    }

    private suspend fun runRoot(
        commandText: String,
        outputFlow: MutableStateFlow<List<OutputLine>>
    ) {
        withContext(Dispatchers.IO) {
            try {
                if (rootShell == null || rootShell?.isAlive != true) {
                    rootShell = Shell.Builder.create().apply {
                        setFlags(Shell.FLAG_MOUNT_MASTER)
                    }.build()
                }

                if (rootShell?.isRoot == false) {
                    return@withContext
                }

                val job = rootShell?.newJob()
                val result = job?.add(commandText)?.exec()

                result?.out?.forEach { line ->
                    outputFlow.update { it + OutputLine(line, isError = false) }
                }

                result?.err?.forEach { line ->
                    outputFlow.update { it + OutputLine(line, isError = true) }
                }

            } catch (e: Exception) {
                outputFlow.update { it + OutputLine("Root shell error: ${e.message}", true) }
            }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun runShizuku(
        commandText: String,
        outputFlow: MutableStateFlow<List<OutputLine>>
    ) {
        withContext(Dispatchers.IO) {
            try {
                val process = Shizuku.newProcess(arrayOf("sh", "-c", commandText), null, currentDir)
                val stdout = BufferedReader(InputStreamReader(process.inputStream))
                val stderr = BufferedReader(InputStreamReader(process.errorStream))

                var line: String?

                while (stdout.readLine().also { line = it } != null) {
                    outputFlow.update { it + OutputLine(line ?: "", isError = false) }
                }

                while (stderr.readLine().also { line = it } != null) {
                    outputFlow.update { it + OutputLine(line ?: "", isError = true) }
                }

                process.waitFor()

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

    private suspend fun runGenericProcess(
        process: Process,
        outputFlow: MutableStateFlow<List<OutputLine>>
    ) {
        currentProcess = process

        val reader = process.inputStream.bufferedReader()
        val errReader = process.errorStream.bufferedReader()

        val stdJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                reader.forEachLine { line ->
                    outputFlow.update { it + OutputLine(line, false) }
                }
            } catch (_: InterruptedIOException) {
            } catch (e: Exception) {
                outputFlow.update { it + OutputLine("Std stream error: ${e.message}", true) }
            }
        }

        val errJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                errReader.forEachLine { line ->
                    outputFlow.update { it + OutputLine(line, true) }
                }
            } catch (_: InterruptedIOException) {
            } catch (e: Exception) {
                outputFlow.update { it + OutputLine("Err stream error: ${e.message}", true) }
            }
        }

        stdJob.join()
        errJob.join()
        process.waitFor()
        currentProcess = null
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