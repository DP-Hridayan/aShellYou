package `in`.hridayan.ashell.qstiles.data.executor

import android.content.ContextWrapper
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.core.common.domain.repository.TcpIpAdbRepository
import `in`.hridayan.ashell.qstiles.domain.model.TileErrorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TcpIpExecutorTest {

    private lateinit var fakeRepository: FakeTcpIpAdbRepository
    private lateinit var executor: TcpIpExecutor

    @Before
    fun setUp() {
        fakeRepository = FakeTcpIpAdbRepository()
        executor = TcpIpExecutor(
            tcpIpAdbRepository = fakeRepository,
            context = ContextWrapper(null)
        ).apply {
            isDeveloperOptionsEnabled = { true }
            isUsbDebuggingEnabled = { true }
            isTcpModeAvailable = { true }
            getAdbTcpPort = { 5555 }
        }
    }

    @Test
    fun execute_whenDeveloperOptionsDisabled_returnsPermissionDenied() = runTest {
        executor.isDeveloperOptionsEnabled = { false }

        val result = executor.execute("echo test")

        assertFalse(result.isSuccess)
        assertEquals(TileErrorType.PERMISSION_DENIED, result.errorType)
        assertTrue(result.output.contains("Developer options are disabled"))
    }

    @Test
    fun execute_whenUsbDebuggingDisabled_returnsPermissionDenied() = runTest {
        executor.isUsbDebuggingEnabled = { false }

        val result = executor.execute("echo test")

        assertFalse(result.isSuccess)
        assertEquals(TileErrorType.PERMISSION_DENIED, result.errorType)
        assertTrue(result.output.contains("USB debugging is disabled"))
    }

    @Test
    fun execute_whenTcpModeUnavailable_returnsPermissionDenied() = runTest {
        executor.isTcpModeAvailable = { false }

        val result = executor.execute("echo test")

        assertFalse(result.isSuccess)
        assertEquals(TileErrorType.PERMISSION_DENIED, result.errorType)
        assertTrue(result.output.contains("TCP mode not available"))
    }

    @Test
    fun execute_whenNotConnectedAndConnectFails_returnsExecutionFailed() = runTest {
        fakeRepository.isConnectedResult = false
        fakeRepository.shouldConnectSucceed = false

        val result = executor.execute("echo test")

        assertFalse(result.isSuccess)
        assertEquals(TileErrorType.EXECUTION_FAILED, result.errorType)
        assertTrue(result.output.contains("Connection to port 5555 failed"))
    }

    @Test
    fun execute_whenConnectedAndSuccess_returnsOutputAndSuccess() = runTest {
        fakeRepository.isConnectedResult = true
        fakeRepository.emittedLines = listOf(
            OutputLine("Line 1", isError = false),
            OutputLine("Line 2", isError = false)
        )

        val result = executor.execute("adb shell echo hello")

        assertTrue(result.isSuccess)
        assertEquals(TileErrorType.NONE, result.errorType)
        assertEquals("echo hello", fakeRepository.lastExecutedCommand)
        assertEquals("Line 1\nLine 2", result.output)
    }

    @Test
    fun execute_whenOutputContainsError_returnsExecutionFailed() = runTest {
        fakeRepository.isConnectedResult = true
        fakeRepository.emittedLines = listOf(
            OutputLine("Unknown error", isError = true)
        )

        val result = executor.execute("ls /nonexistent")

        assertFalse(result.isSuccess)
        assertEquals(TileErrorType.EXECUTION_FAILED, result.errorType)
        assertEquals("Unknown error", result.output)
    }
}

private class FakeTcpIpAdbRepository : TcpIpAdbRepository {
    var isConnectedResult = false
    var shouldConnectSucceed = true
    var lastConnectedIp: String? = null
    var lastConnectedPort: Int = -1
    var lastExecutedCommand: String? = null
    var emittedLines = listOf<OutputLine>()
    var abortCalled = false

    override fun connect(ip: String?, port: Int, callback: TcpIpAdbRepository.TcpIpConnectionListener?) {
        lastConnectedIp = ip
        lastConnectedPort = port
        if (shouldConnectSucceed) {
            isConnectedResult = true
            callback?.onConnectionSuccess()
        } else {
            callback?.onConnectionFailed()
        }
    }

    override fun execute(commandText: String): Flow<OutputLine> = flow {
        lastExecutedCommand = commandText
        emittedLines.forEach { emit(it) }
    }

    override fun isConnected(): Boolean = isConnectedResult

    override fun abortShell() {
        abortCalled = true
    }
}
