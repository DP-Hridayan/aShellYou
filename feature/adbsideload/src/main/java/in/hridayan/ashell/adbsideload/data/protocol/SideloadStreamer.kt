package `in`.hridayan.ashell.adbsideload.data.protocol

import android.content.Context
import android.net.Uri
import com.cgutman.adblib.AdbConnection
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.adbsideload.data.file.SideloadPackage
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadError
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus
import `in`.hridayan.ashell.adbsideload.domain.protocol.SideloadHostSession
import `in`.hridayan.ashell.adbsideload.domain.protocol.SideloadProgressTracker
import `in`.hridayan.ashell.adbsideload.domain.protocol.SideloadTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Opens the package and the `sideload-host` stream on an established connection, then hands both
 * to a [SideloadHostSession].
 */
@Singleton
class SideloadStreamer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private sealed interface TransportResult {
        class Ready(val transport: SideloadTransport) : TransportResult
        class Failure(val error: SideloadError, val detail: String?) : TransportResult
    }

    @Volatile
    private var activeTransport: SideloadTransport? = null

    fun stream(connection: AdbConnection, uri: Uri): Flow<SideloadOperation> = flow {
        val fileName = SideloadPackage.resolveName(context, uri)
        emit(SideloadOperation(fileName = fileName, status = SideloadStatus.READING_FILE))
        val sideloadPackage = SideloadPackage.open(context, uri)
        if (sideloadPackage == null) {
            emit(failure(fileName, SideloadError.FILE_OPEN_FAILED))
        } else {
            sideloadPackage.use { emitAll(serve(connection, it)) }
        }
    }.flowOn(Dispatchers.IO)

    fun cancel() {
        activeTransport?.close()
    }

    private suspend fun serve(connection: AdbConnection, sideloadPackage: SideloadPackage): Flow<SideloadOperation> {
        if (sideloadPackage.size <= 0L) {
            return flowOf(failure(sideloadPackage.name, SideloadError.FILE_SIZE_UNKNOWN))
        }
        val tracker = SideloadProgressTracker(sideloadPackage.name, sideloadPackage.size, BLOCK_SIZE)
        return when (val result = openTransport(connection, sideloadPackage.size)) {
            is TransportResult.Ready -> runSession(result.transport, sideloadPackage, tracker)
            is TransportResult.Failure -> flowOf(tracker.failure(result.error, result.detail))
        }
    }

    private fun runSession(
        transport: SideloadTransport,
        sideloadPackage: SideloadPackage,
        tracker: SideloadProgressTracker,
    ): Flow<SideloadOperation> {
        activeTransport = transport
        return SideloadHostSession(transport, sideloadPackage.reader, tracker, BLOCK_SIZE)
            .run()
            .onCompletion {
                transport.close()
                activeTransport = null
            }
    }

    private suspend fun openTransport(connection: AdbConnection, fileSize: Long): TransportResult {
        val service = "$SIDELOAD_HOST_SERVICE:$fileSize:$BLOCK_SIZE"
        return try {
            val stream = withTimeout(OPEN_TIMEOUT_MS) {
                runInterruptible(Dispatchers.IO) { connection.open(service) }
            }
            val maxWrite = runInterruptible(Dispatchers.IO) { connection.negotiatedMaxPayload }
            TransportResult.Ready(AdbStreamTransport(stream, minOf(maxWrite, BLOCK_SIZE)))
        } catch (e: TimeoutCancellationException) {
            TransportResult.Failure(SideloadError.RECOVERY_NOT_RESPONDING, e.message)
        } catch (e: IOException) {
            TransportResult.Failure(SideloadError.STREAM_OPEN_FAILED, e.message)
        }
    }

    private fun failure(fileName: String, error: SideloadError): SideloadOperation =
        SideloadOperation(fileName = fileName, status = SideloadStatus.ERROR, error = error)

    private companion object {
        const val BLOCK_SIZE = SideloadHostSession.DEFAULT_BLOCK_SIZE
        const val SIDELOAD_HOST_SERVICE = "sideload-host"
        const val OPEN_TIMEOUT_MS = 30_000L
    }
}
