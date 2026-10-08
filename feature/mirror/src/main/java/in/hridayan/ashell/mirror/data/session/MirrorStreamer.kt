package `in`.hridayan.ashell.mirror.data.session

import `in`.hridayan.ashell.mirror.data.connection.ScrcpySockets
import `in`.hridayan.ashell.mirror.data.control.ControlLink
import `in`.hridayan.ashell.mirror.data.decoder.StreamCounters
import `in`.hridayan.ashell.mirror.data.decoder.SurfaceVideoOutput
import `in`.hridayan.ashell.mirror.data.decoder.VideoRenderer
import `in`.hridayan.ashell.mirror.data.decoder.VideoStreamReader
import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.MirrorError
import `in`.hridayan.ashell.mirror.domain.model.StreamStats
import `in`.hridayan.ashell.mirror.domain.model.VideoOutput
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

private const val STATS_INTERVAL_MS = 1_000L

/**
 * Moves video from the device to the screen and input from the screen to the device, for one
 * connected session at a time.
 *
 * The render target can come and go independently of the session, for example while the activity
 * is recreated, so it is remembered here and handed to each session's renderer.
 */
class MirrorStreamer(private val ioDispatcher: CoroutineDispatcher) {

    private val controlLink = ControlLink(ioDispatcher)

    @Volatile
    private var renderer: VideoRenderer? = null

    @Volatile
    private var videoOutput: VideoOutput? = null

    fun send(message: ControlMessage) = controlLink.send(message)

    /** Stops forwarding input for the rest of the session, after the device refused injection. */
    fun disableInput() {
        controlLink.isAccepting = false
    }

    fun setVideoOutput(output: VideoOutput?) {
        videoOutput = output
        renderer?.setSurface(output.surfaceOrNull())
    }

    /**
     * Streams until the video socket closes or this side fails.
     *
     * @param onStats called once a second with the last second's statistics.
     * @return an error raised on this side, or null when the device closed the stream.
     */
    suspend fun stream(
        sockets: ScrcpySockets,
        onDeviceName: (String) -> Unit,
        onSession: (VideoSize) -> Unit,
        onStats: (StreamStats) -> Unit
    ): MirrorError? = coroutineScope {
        val decoderError = AtomicReference<Throwable?>(null)
        val counters = StreamCounters()
        val activeRenderer = VideoRenderer(
            onKeyFrameNeeded = { send(ControlMessage.ResetVideo) },
            onDecoderError = decoderError::set,
            counters = counters
        )
        val reader = VideoStreamReader(activeRenderer, decoderError, onDeviceName, onSession)

        renderer = activeRenderer
        activeRenderer.setSurface(videoOutput.surfaceOrNull())
        val control = launch { controlLink.pump(sockets.control) }
        val sampler = launch {
            while (isActive) {
                delay(STATS_INTERVAL_MS)
                onStats(counters.drain())
            }
        }

        try {
            withContext(ioDispatcher) { reader.read(sockets.video) }
        } finally {
            control.cancel()
            sampler.cancel()
            renderer = null
            activeRenderer.release()
        }
    }

    private fun VideoOutput?.surfaceOrNull() = (this as? SurfaceVideoOutput)?.surface
}
