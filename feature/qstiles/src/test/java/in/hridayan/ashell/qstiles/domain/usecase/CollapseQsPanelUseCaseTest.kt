package `in`.hridayan.ashell.qstiles.domain.usecase

import `in`.hridayan.ashell.core.common.domain.model.TileExecutionMode
import `in`.hridayan.ashell.qstiles.data.usecase.CollapseQsPanelUseCaseImpl
import `in`.hridayan.ashell.qstiles.domain.executor.CommandExecutor
import `in`.hridayan.ashell.qstiles.domain.executor.CommandResult
import `in`.hridayan.ashell.qstiles.domain.model.TileErrorType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CollapseQsPanelUseCaseTest {

    private lateinit var fakeShizukuExecutor: FakeCommandExecutor
    private lateinit var fakeRootExecutor: FakeCommandExecutor
    private lateinit var fakeTcpIpExecutor: FakeCommandExecutor
    private lateinit var useCase: CollapseQsPanelUseCase

    @Before
    fun setUp() {
        fakeShizukuExecutor = FakeCommandExecutor()
        fakeRootExecutor = FakeCommandExecutor()
        fakeTcpIpExecutor = FakeCommandExecutor()
        useCase = CollapseQsPanelUseCaseImpl(fakeShizukuExecutor, fakeRootExecutor, fakeTcpIpExecutor)
    }

    @Test
    fun invoke_withShizukuMode_executesCommandViaShizuku() = runTest {
        val result = useCase(TileExecutionMode.SHIZUKU)

        assertTrue(result)
        assertEquals("cmd statusbar collapse", fakeShizukuExecutor.lastExecutedCommand)
        assertEquals(null, fakeRootExecutor.lastExecutedCommand)
        assertEquals(null, fakeTcpIpExecutor.lastExecutedCommand)
    }

    @Test
    fun invoke_withRootMode_executesCommandViaRoot() = runTest {
        val result = useCase(TileExecutionMode.ROOT)

        assertTrue(result)
        assertEquals("cmd statusbar collapse", fakeRootExecutor.lastExecutedCommand)
        assertEquals(null, fakeShizukuExecutor.lastExecutedCommand)
        assertEquals(null, fakeTcpIpExecutor.lastExecutedCommand)
    }

    @Test
    fun invoke_withTcpIpMode_executesCommandViaTcpIp() = runTest {
        val result = useCase(TileExecutionMode.TCPIP)

        assertTrue(result)
        assertEquals("cmd statusbar collapse", fakeTcpIpExecutor.lastExecutedCommand)
        assertEquals(null, fakeShizukuExecutor.lastExecutedCommand)
        assertEquals(null, fakeRootExecutor.lastExecutedCommand)
    }

    @Test
    fun invoke_withInvalidMode_returnsFalseWithoutExecution() = runTest {
        val result = useCase(999)

        assertFalse(result)
        assertEquals(null, fakeShizukuExecutor.lastExecutedCommand)
        assertEquals(null, fakeRootExecutor.lastExecutedCommand)
        assertEquals(null, fakeTcpIpExecutor.lastExecutedCommand)
    }

    @Test
    fun invoke_whenCommandFails_returnsFalse() = runTest {
        fakeShizukuExecutor.shouldSucceed = false

        val result = useCase(TileExecutionMode.SHIZUKU)

        assertFalse(result)
    }
}

private class FakeCommandExecutor : CommandExecutor {
    var lastExecutedCommand: String? = null
    var shouldSucceed: Boolean = true

    override suspend fun execute(command: String): CommandResult {
        lastExecutedCommand = command
        return CommandResult(
            output = if (shouldSucceed) "Success" else "Failed",
            isSuccess = shouldSucceed,
            errorType = if (shouldSucceed) TileErrorType.NONE else TileErrorType.EXECUTION_FAILED,
            durationMs = 10L
        )
    }
}
