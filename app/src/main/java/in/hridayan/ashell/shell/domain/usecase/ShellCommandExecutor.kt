package `in`.hridayan.ashell.shell.domain.usecase

import `in`.hridayan.ashell.shell.domain.model.OutputLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InterruptedIOException

class ShellCommandExecutor {

    private var currentProcess: Process? = null

    fun stop() {
        currentProcess?.destroy()
        currentProcess = null
    }

    suspend fun executeCommand(
        commandText: String,
        outputFlow: MutableStateFlow<List<OutputLine>>
    ) {
        withContext(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec(commandText)
                currentProcess = process

                val reader = process.inputStream.bufferedReader()
                val errReader = process.errorStream.bufferedReader()

                val stdJob = CoroutineScope(Dispatchers.IO).launch {
                    try {
                        reader.forEachLine { line ->
                            outputFlow.update { it + OutputLine(line, isError = false) }
                        }
                    } catch (_: InterruptedIOException) {
                    } catch (e: Exception) {
                        outputFlow.update { it + OutputLine("Std stream error: ${e.message}", true) }
                    }
                }

                val errJob = CoroutineScope(Dispatchers.IO).launch {
                    try {
                        errReader.forEachLine { line ->
                            outputFlow.update { it + OutputLine(line, isError = true) }
                        }
                    } catch (_: InterruptedIOException) {
                    } catch (e: Exception) {
                        outputFlow.update { it + OutputLine("Err stream error: ${e.message}", true) }
                    }
                }

                stdJob.join()
                errJob.join()
                process.waitFor()
            } catch (e: Exception) {
                outputFlow.update { it + OutputLine("Process error: ${e.message}", true) }
            } finally {
                currentProcess = null
            }
        }
    }
}
