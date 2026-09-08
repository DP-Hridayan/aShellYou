package `in`.hridayan.ashell.logcat.domain.usecase

import `in`.hridayan.ashell.core.common.domain.model.LogcatWorkingMode
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.core.common.domain.repository.ShellRepository
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.Ready
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.RootUnavailable
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.ShizukuPermissionDenied
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.ShizukuUnavailable
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.WirelessNotConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performs the mode-specific pre-flight permission/availability check before
 * starting the logcat service.
 *
 * Callers should invoke [check] from a coroutine — [RootUnavailable] check
 * runs `su id` on [Dispatchers.IO] and may take ~1 s on first call.
 */
@Singleton
class CheckLogcatPreflightUseCase @Inject constructor(
    private val shellRepository: ShellRepository,
    private val checkReadLogs: CheckReadLogsPreflightUseCase,
) {
    suspend fun check(mode: Int): LogcatPreflightResult = when (mode) {
        LogcatWorkingMode.READ_LOGS -> checkReadLogs()
        LogcatWorkingMode.SHIZUKU -> checkShizuku()
        LogcatWorkingMode.ROOT -> checkRoot()
        LogcatWorkingMode.WIRELESS -> checkWireless()
        else -> Ready
    }

    private fun checkShizuku(): LogcatPreflightResult = when {
        !Shizuku.pingBinder() -> ShizukuUnavailable
        !shellRepository.hasShizukuPermission() -> ShizukuPermissionDenied
        else -> Ready
    }

    private suspend fun checkRoot(): LogcatPreflightResult {
        val hasRoot = withContext(Dispatchers.IO) { shellRepository.hasRootAccess() }
        return if (hasRoot) Ready else RootUnavailable
    }

    private fun checkWireless(): LogcatPreflightResult {
        val device = WifiAdbConnection.currentDevice.value
        val state = WifiAdbConnection.currentState
        val connected = device?.isOwnDevice == true && state is WifiAdbState.Connected
        return if (connected) Ready else WirelessNotConnected
    }
}
