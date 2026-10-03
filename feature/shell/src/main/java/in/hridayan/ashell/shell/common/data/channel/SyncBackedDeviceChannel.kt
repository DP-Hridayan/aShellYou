package `in`.hridayan.ashell.shell.common.data.channel

import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannel
import `in`.hridayan.ashell.core.common.domain.model.RemoteFileStat
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncSession
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncTransport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import java.io.InputStream

private const val SYNC_SERVICE = "sync:"

/**
 * The transport-independent half of an [ExternalDeviceChannel]: file metadata and uploads run on the
 * existing [SyncSession], so subclasses only adapt their ADB library's stream type.
 */
abstract class SyncBackedDeviceChannel : ExternalDeviceChannel {

    /** Opens [service] on the connected device; may block until the device answers the open. */
    protected abstract suspend fun openRaw(service: String): AdbDuplexStream

    protected abstract suspend fun openSyncTransport(service: String): SyncTransport

    override suspend fun openStream(service: String): Result<AdbDuplexStream> =
        resultOf { openRaw(service) }

    override suspend fun stat(remotePath: String): Result<RemoteFileStat> = resultOf {
        withSyncSession { session ->
            val stat = session.stat(remotePath)
            RemoteFileStat(sizeBytes = stat.size, exists = stat.exists)
        }
    }

    override suspend fun push(source: InputStream, remotePath: String, mode: Int): Result<Unit> =
        resultOf {
            withSyncSession { session -> session.push(source, remotePath, mode) {} }
        }

    private suspend fun <T> withSyncSession(block: suspend (SyncSession) -> T): T {
        val transport = openSyncTransport(SYNC_SERVICE)
        val session = SyncSession(transport)
        return try {
            block(session).also { runCatching { session.quit() } }
        } finally {
            transport.close()
        }
    }

    /**
     * A timeout inside a sync session is a failure of this call, not a cancellation of the caller,
     * even though [TimeoutCancellationException] is a [CancellationException]. Rethrowing it would
     * silently end whatever coroutine asked, as if the user had left.
     */
    private suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: TimeoutCancellationException) {
        Result.failure(e)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
