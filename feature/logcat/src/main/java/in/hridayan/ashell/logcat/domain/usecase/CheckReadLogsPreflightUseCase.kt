package `in`.hridayan.ashell.logcat.domain.usecase

import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.NeedsReadLogs
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.NeedsRestartForReadLogs
import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult.Ready
import `in`.hridayan.ashell.logcat.domain.permission.ReadLogsAccessChecker
import javax.inject.Inject

class CheckReadLogsPreflightUseCase @Inject constructor(
    private val accessChecker: ReadLogsAccessChecker,
) {
    operator fun invoke(): LogcatPreflightResult = when {
        !accessChecker.isPermissionGranted() -> NeedsReadLogs
        !processHasLogGroup() -> NeedsRestartForReadLogs
        else -> Ready
    }

    private fun processHasLogGroup(): Boolean = accessChecker.hasLogGroup().getOrDefault(true)
}
