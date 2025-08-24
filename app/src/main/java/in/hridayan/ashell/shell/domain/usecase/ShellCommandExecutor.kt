package `in`.hridayan.ashell.shell.domain.usecase

import android.content.Context
import `in`.hridayan.ashell.shell.domain.model.OutputLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.InterruptedIOException

class ShellCommandExecutor {
    private var currentProcess: Process? = null
    private var shizukuProcess: ShizukuRemoteProcess? = null
    private var currentDir = "/"

    fun runBasic(commandText: String, context: Context): Flow<OutputLine> = flow {
        val busyboxFile = File(context.filesDir, "busybox")

        val process =
            Runtime.getRuntime().exec(arrayOf(busyboxFile.absolutePath, "sh", "-c", commandText))
        emitAll(exec(process))
    }.flowOn(Dispatchers.IO)

    fun runRoot(commandText: String): Flow<OutputLine> = flow {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", commandText))
        emitAll(exec(process))
    }.flowOn(Dispatchers.IO)


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

        shizukuProcess?.let {
            emitAll(execShizukuProcess())
        }
    }.flowOn(Dispatchers.IO)

    fun execShizukuProcess(): Flow<OutputLine> = flow {
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
}