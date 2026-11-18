package `in`.hridayan.ashell.qstiles.data.executor

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.repository.TcpIpAdbRepository
import `in`.hridayan.ashell.core.utils.TcpIpUtils
import `in`.hridayan.ashell.qstiles.domain.executor.CommandExecutor
import `in`.hridayan.ashell.qstiles.domain.executor.CommandResult
import `in`.hridayan.ashell.qstiles.domain.model.TileErrorType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class TcpIpExecutor @Inject constructor(
    private val tcpIpAdbRepository: TcpIpAdbRepository,
    @param:ApplicationContext private val context: Context,
) : CommandExecutor {

    internal var isDeveloperOptionsEnabled: () -> Boolean = {
        TcpIpUtils.isDeveloperOptionsEnabled(context)
    }

    internal var isUsbDebuggingEnabled: () -> Boolean = {
        TcpIpUtils.isUsbDebuggingEnabled(context)
    }

    internal var isTcpModeAvailable: () -> Boolean = {
        TcpIpUtils.isTcpModeAvailable()
    }

    internal var getAdbTcpPort: () -> Int = {
        TcpIpUtils.getAdbTcpPort()
    }

    override suspend fun execute(command: String): CommandResult = withContext(Dispatchers.IO) {
        val filteredCommand = filterCommand(command)
        val start = System.currentTimeMillis()

        if (!isDeveloperOptionsEnabled()) {
            return@withContext CommandResult(
                output = "Developer options are disabled.",
                isSuccess = false,
                errorType = TileErrorType.PERMISSION_DENIED,
                durationMs = elapsed(start)
            )
        }

        if (!isUsbDebuggingEnabled()) {
            return@withContext CommandResult(
                output = "USB debugging is disabled.",
                isSuccess = false,
                errorType = TileErrorType.PERMISSION_DENIED,
                durationMs = elapsed(start)
            )
        }

        if (!isTcpModeAvailable()) {
            return@withContext CommandResult(
                output = "TCP mode not available. ADB TCP port is not open.",
                isSuccess = false,
                errorType = TileErrorType.PERMISSION_DENIED,
                durationMs = elapsed(start)
            )
        }

        val port = getAdbTcpPort()

        if (!tcpIpAdbRepository.isConnected()) {
            val connected = suspendCancellableCoroutine { continuation ->
                tcpIpAdbRepository.connect(
                    ip = LOOPBACK_HOST,
                    port = port,
                    callback = object : TcpIpAdbRepository.TcpIpConnectionListener {
                        override fun onConnectionSuccess() {
                            if (continuation.isActive) continuation.resume(true)
                        }

                        override fun onConnectionFailed() {
                            if (continuation.isActive) continuation.resume(false)
                        }
                    }
                )
            }

            if (!connected) {
                return@withContext CommandResult(
                    output = "Connection to port $port failed. The ADB daemon may not be listening.",
                    isSuccess = false,
                    errorType = TileErrorType.EXECUTION_FAILED,
                    durationMs = elapsed(start)
                )
            }
        }

        try {
            val output = StringBuilder()
            val errorOutput = StringBuilder()
            var hasError = false

            tcpIpAdbRepository.execute(filteredCommand).collect { line ->
                if (line.isError) {
                    hasError = true
                    errorOutput.appendLine(line.text)
                } else {
                    output.appendLine(line.text)
                }
            }

            val combinedOutput = buildString {
                if (output.isNotBlank()) append(output.trim())
                if (errorOutput.isNotBlank()) {
                    if (isNotEmpty()) appendLine()
                    append(errorOutput.trim())
                }
            }

            CommandResult(
                output = combinedOutput.ifBlank { "Command executed (no output)." },
                isSuccess = !hasError,
                errorType = if (!hasError) TileErrorType.NONE else TileErrorType.EXECUTION_FAILED,
                durationMs = elapsed(start)
            )
        } catch (e: CancellationException) {
            tcpIpAdbRepository.abortShell()
            throw e
        } catch (e: Exception) {
            tcpIpAdbRepository.abortShell()
            CommandResult(
                output = "TCP/IP execution error: ${e.message}",
                isSuccess = false,
                errorType = TileErrorType.EXECUTION_FAILED,
                durationMs = elapsed(start)
            )
        }
    }

    private fun filterCommand(command: String): String {
        return command.trim().removePrefix("adb ").removePrefix("shell ")
    }

    private fun elapsed(start: Long) = System.currentTimeMillis() - start

    private companion object {
        const val LOOPBACK_HOST = "127.0.0.1"
    }
}
