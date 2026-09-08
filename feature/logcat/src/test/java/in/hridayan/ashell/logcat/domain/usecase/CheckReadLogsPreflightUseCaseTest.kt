package `in`.hridayan.ashell.logcat.domain.usecase

import `in`.hridayan.ashell.logcat.domain.model.LogcatPreflightResult
import `in`.hridayan.ashell.logcat.domain.permission.ReadLogsAccessChecker
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeReadLogsAccessChecker(
    private val granted: Boolean,
    private val logGroup: Result<Boolean>,
) : ReadLogsAccessChecker {
    override fun isPermissionGranted(): Boolean = granted
    override fun hasLogGroup(): Result<Boolean> = logGroup
}

class CheckReadLogsPreflightUseCaseTest {

    @Test
    fun `missing permission needs read logs`() {
        val useCase = CheckReadLogsPreflightUseCase(FakeReadLogsAccessChecker(false, Result.success(true)))
        assertEquals(LogcatPreflightResult.NeedsReadLogs, useCase())
    }

    @Test
    fun `granted without log group needs restart`() {
        val useCase = CheckReadLogsPreflightUseCase(FakeReadLogsAccessChecker(true, Result.success(false)))
        assertEquals(LogcatPreflightResult.NeedsRestartForReadLogs, useCase())
    }

    @Test
    fun `granted with log group is ready`() {
        val useCase = CheckReadLogsPreflightUseCase(FakeReadLogsAccessChecker(true, Result.success(true)))
        assertEquals(LogcatPreflightResult.Ready, useCase())
    }

    @Test
    fun `probe failure falls back to ready`() {
        val useCase = CheckReadLogsPreflightUseCase(
            FakeReadLogsAccessChecker(true, Result.failure(IllegalStateException())),
        )
        assertEquals(LogcatPreflightResult.Ready, useCase())
    }
}
