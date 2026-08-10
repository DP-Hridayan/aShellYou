package `in`.hridayan.ashell.logcat.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.core.common.domain.model.LogcatWorkingMode
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.core.common.domain.repository.ShellRepository
import `in`.hridayan.ashell.logcat.data.permission.LogcatPermissionHelper
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.NeedsReadLogs
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
    @ApplicationContext private val context: Context,
) {
    suspend fun check(mode: Int): LogcatPreflightResult = when (mode) {
        LogcatWorkingMode.BASIC -> {
            if (LogcatPermissionHelper.hasReadLogsPermission(context)) {
                Ready
            } else {
                NeedsReadLogs
            }
        }

        LogcatWorkingMode.SHIZUKU -> {
            if (!Shizuku.pingBinder()) {
                ShizukuUnavailable
            } else if (!shellRepository.hasShizukuPermission()) {
                ShizukuPermissionDenied
            } else {
                Ready
            }
        }

        LogcatWorkingMode.ROOT -> {
            val hasRoot = withContext(Dispatchers.IO) { shellRepository.hasRootAccess() }
            if (hasRoot) Ready else RootUnavailable
        }

        LogcatWorkingMode.WIRELESS -> {
            val device = WifiAdbConnection.currentDevice.value
            val state = WifiAdbConnection.currentState
            if (device?.isOwnDevice == true && state is WifiAdbState.Connected) {
                Ready
            } else {
                WirelessNotConnected
            }
        }

        else -> Ready
    }
}
