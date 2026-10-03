package `in`.hridayan.ashell.mirror.data.decoder

import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.mirror.domain.model.MirrorError
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import `in`.hridayan.ashell.mirror.domain.model.VideoStreamEvent
import `in`.hridayan.ashell.mirror.domain.protocol.VideoStreamParser
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

/**
 * Reads the video socket of one session at line rate and hands its contents to a [VideoRenderer].
 *
 * @param decoderError set by the renderer's decoder thread; a failure there ends the read.
 * @param onDeviceName called once, when the device introduces itself.
 * @param onSession called at every capture session, with the size touches must be scaled to.
 */
class VideoStreamReader(
    private val renderer: VideoRenderer,
    private val decoderError: AtomicReference<Throwable?>,
    private val onDeviceName: (String) -> Unit,
    private val onSession: (VideoSize) -> Unit
) {

    /**
     * Blocks the calling coroutine until the stream ends.
     *
     * @return an error that ends the session on this side, or null when the stream simply closed.
     */
    suspend fun read(video: AdbDuplexStream): MirrorError? {
        val parser = VideoStreamParser(expectsHandshake = true)
        return try {
            var error: MirrorError? = null
            while (error == null) {
                error = parser.feed(video.read()).firstNotNullOfOrNull(::handle)
                    ?: decoderError.get()?.let { MirrorError.StreamError }
            }
            error
        } catch (_: VideoStreamParser.ProtocolException) {
            MirrorError.StreamError
        } catch (_: IOException) {
            null
        }
    }

    private fun handle(event: VideoStreamEvent): MirrorError? {
        when (event) {
            is VideoStreamEvent.DeviceName -> onDeviceName(event.name)

            is VideoStreamEvent.Codec -> {
                if (!DecoderSupport.isSupported(event.codec.mimeType)) {
                    return MirrorError.DecoderUnavailable(event.codec)
                }
                renderer.onCodec(event.codec)
            }

            is VideoStreamEvent.Disabled -> return MirrorError.StreamError

            is VideoStreamEvent.Session -> {
                renderer.onSession(event.size)
                onSession(event.size)
            }

            is VideoStreamEvent.Packet -> renderer.onPacket(event.packet)
        }
        return null
    }
}
