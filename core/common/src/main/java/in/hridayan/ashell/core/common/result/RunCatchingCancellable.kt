package `in`.hridayan.ashell.core.common.result

import kotlinx.coroutines.CancellationException

/**
 * Executes [block] and wraps any non-cancellation failure in [Result.failure],
 * rethrowing [CancellationException] so structured concurrency cancellation is preserved.
 */
@Suppress("TooGenericExceptionCaught")
inline fun <R> runCatchingCancellable(block: () -> R): Result<R> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(e)
}
