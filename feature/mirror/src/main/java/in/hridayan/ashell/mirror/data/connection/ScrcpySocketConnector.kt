package `in`.hridayan.ashell.mirror.data.connection

import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannel
import `in`.hridayan.ashell.mirror.domain.protocol.ServerCommandBuilder
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeoutException
import javax.inject.Inject

private const val RETRY_INTERVAL_MS = 100L
private const val ATTEMPT_TIMEOUT_MS = 5_000L
private const val TOTAL_TIMEOUT_MS = 10_000L

/** The two server sockets a control-enabled, audio-less session uses. */
class ScrcpySockets(val video: AdbDuplexStream, val control: AdbDuplexStream) {
    fun close() {
        video.close()
        control.close()
    }
}

/**
 * Connects to the server's abstract socket through the device's adbd, the forward-tunnel mode that
 * needs no adb server on this side.
 *
 * The server accepts its sockets in a fixed order, so video is opened before control. It only
 * listens once `app_process` has started, so the first socket is retried until it is accepted.
 */
class ScrcpySocketConnector @Inject constructor() {

    suspend fun connect(channel: ExternalDeviceChannel, scid: Int): Result<ScrcpySockets> {
        val service = ServerCommandBuilder.socketService(scid)

        val video = openWithRetry(channel, service)
            ?: return Result.failure(TimeoutException("The server never accepted a connection"))

        val control = openOnce(channel, service)
        if (control == null) {
            video.close()
            return Result.failure(TimeoutException("The server did not accept the control socket"))
        }

        return Result.success(ScrcpySockets(video, control))
    }

    /**
     * Like [connect], but gives up as soon as [serverExited] completes: a server that already quit,
     * for example over a version mismatch, will never listen, so waiting out the timeout is pointless.
     */
    suspend fun connectUnless(
        serverExited: Deferred<Unit>,
        channel: ExternalDeviceChannel,
        scid: Int
    ): ScrcpySockets? = coroutineScope {
        val connecting = async { connect(channel, scid).getOrNull() }
        val sockets = select {
            connecting.onAwait { it }
            serverExited.onAwait { null }
        }
        if (sockets == null) connecting.cancel()
        sockets
    }

    private suspend fun openWithRetry(channel: ExternalDeviceChannel, service: String): AdbDuplexStream? =
        withTimeoutOrNull(TOTAL_TIMEOUT_MS) {
            var stream = openOnce(channel, service)
            while (stream == null) {
                delay(RETRY_INTERVAL_MS)
                stream = openOnce(channel, service)
            }
            stream
        }

    /**
     * Bounded per attempt: the Wi-Fi library can miss a rejection and wait forever, and only an
     * interrupt from this timeout gets its thread back.
     */
    private suspend fun openOnce(channel: ExternalDeviceChannel, service: String): AdbDuplexStream? =
        withTimeoutOrNull(ATTEMPT_TIMEOUT_MS) { channel.openStream(service).getOrNull() }
}
