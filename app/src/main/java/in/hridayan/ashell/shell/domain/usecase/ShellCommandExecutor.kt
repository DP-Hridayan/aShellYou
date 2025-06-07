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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedReader
import java.io.InputStreamReader

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
        try {
            if (rootShell == null || rootShell?.isAlive != true) {
                rootShell = Shell.Builder.create().apply {
                    setFlags(FLAG_MOUNT_MASTER)
                }.build()
            }

            if (rootShell?.isRoot == false) {
                return
            }

            runGenericProcess(
                Runtime.getRuntime().exec(arrayOf("su", "-c", commandText)), outputFlow
            )

        } catch (e: Exception) {
            outputFlow.update { it + OutputLine("Root shell error: ${e.message}", true) }
        }
    }

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

    private suspend fun runGenericProcess(
        process: Process,
        outputFlow: MutableStateFlow<List<OutputLine>>
    ) {
        withContext(Dispatchers.IO) {
            try {
                currentProcess = process

                val inputReader = BufferedReader(InputStreamReader(process.inputStream))
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))

                val stdoutJob = launch {
                    var line: String?
                    while (inputReader.readLine().also { line = it } != null) {
                        line?.let {
                            outputFlow.update { currentList ->
                                currentList + OutputLine(it, isError = false)
                            }
                        }
                    }
                }

                val stderrJob = launch {
                    var line: String?
                    while (errorReader.readLine().also { line = it } != null) {
                        line?.let {
                            outputFlow.update { currentList ->
                                currentList + OutputLine(it, isError = true)
                            }
                        }
                    }
                }

                stdoutJob.join()
                stderrJob.join()
                currentProcess?.waitFor()

            } catch (e: Exception) {
                outputFlow.update { currentList ->
                    currentList + OutputLine(
                        "Error executing command: ${e.message}",
                        isError = true
                    )
                }
            }
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