package `in`.hridayan.ashell.core.common.result

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RunCatchingCancellableTest {

    @Test
    fun `returns success when block completes normally`() {
        val result = runCatchingCancellable { 42 }

        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull())
    }

    @Test
    fun `returns failure when block throws non-cancellation exception`() {
        val failure = IOException("disk full")

        val result = runCatchingCancellable<Unit> { throw failure }

        assertTrue(result.isFailure)
        assertEquals(failure, result.exceptionOrNull())
    }

    @Test(expected = CancellationException::class)
    fun `rethrows CancellationException instead of wrapping in failure`() {
        runCatchingCancellable<Unit> { throw CancellationException("job cancelled") }
    }

    @Test
    fun `coroutine cancellation propagates cleanly out of runCatchingCancellable`() = runTest {
        var reachedCatch = false

        val deferred = async {
            runCatchingCancellable {
                awaitCancellation()
            }.onFailure {
                reachedCatch = true
            }
        }

        testScheduler.advanceUntilIdle()
        deferred.cancel()
        testScheduler.advanceUntilIdle()

        assertTrue(deferred.isCancelled)
        assertTrue(!reachedCatch)
    }
}
