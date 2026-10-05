package `in`.hridayan.ashell.mirror.data.decoder

import android.os.SystemClock
import android.view.Surface
import `in`.hridayan.ashell.mirror.domain.model.MediaPacket
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.model.VideoSize

private const val KEY_FRAME_REQUEST_INTERVAL_MS = 1_000L

/**
 * Decides which packets reach a decoder, across surface changes, rotations and dropped frames.
 *
 * Every packet must be consumed as it arrives, whether or not anything is shown: neither ADB library
 * applies backpressure, so unread video would queue in memory without limit. Packets that cannot be
 * shown are discarded instead, and display resumes from the next config packet and key frame, which
 * [onKeyFrameNeeded] asks the server for.
 */
class VideoRenderer(
    private val onKeyFrameNeeded: () -> Unit,
    private val onDecoderError: (Throwable) -> Unit,
    private val counters: StreamCounters
) {

    private val lock = Any()
    private var surface: Surface? = null
    private var codec: VideoCodec? = null
    private var size: VideoSize? = null
    private var decoder: DecoderInstance? = null
    private var awaitingConfig = true
    private var awaitingKeyFrame = false
    private var lastKeyFrameRequestAt = 0L

    fun setSurface(newSurface: Surface?): Unit = synchronized(lock) {
        if (surface === newSurface) return

        releaseDecoderLocked()
        surface = newSurface
        awaitingConfig = true
        if (newSurface != null && codec != null) requestKeyFrameLocked()
    }

    fun onCodec(newCodec: VideoCodec) = synchronized(lock) { codec = newCodec }

    fun onSession(newSize: VideoSize) = synchronized(lock) {
        size = newSize
        releaseDecoderLocked()
        awaitingConfig = true
    }

    fun onPacket(packet: MediaPacket): Unit = synchronized(lock) {
        counters.onReceived(packet.data.size)
        val target = surface ?: return

        if (packet.isConfig) {
            startDecoderLocked(target, packet)
            return
        }

        val active = decoder
        val needsKeyFrame = awaitingKeyFrame && !packet.isKeyFrame
        if (active == null || awaitingConfig || needsKeyFrame) {
            counters.onDropped()
            requestKeyFrameLocked()
            return
        }

        if (active.queue(packet)) {
            if (packet.isKeyFrame) awaitingKeyFrame = false
        } else {
            counters.onDropped()
            awaitingKeyFrame = true
            requestKeyFrameLocked()
        }
    }

    fun release() = synchronized(lock) {
        releaseDecoderLocked()
        surface = null
    }

    /**
     * A new config packet starts every capture session and every requested reset, so the decoder is
     * rebuilt from it rather than fed new parameters mid-stream.
     */
    private fun startDecoderLocked(target: Surface, config: MediaPacket) {
        val mimeType = codec?.mimeType ?: return
        val videoSize = size ?: return
        releaseDecoderLocked()
        decoder = runCatching {
            DecoderInstance(mimeType, videoSize, target, onDecoderError, counters::onRendered).also {
                it.queue(config)
            }
        }.onFailure(onDecoderError).getOrNull()
        awaitingConfig = decoder == null
        awaitingKeyFrame = false
    }

    private fun releaseDecoderLocked() {
        decoder?.release()
        decoder = null
    }

    /**
     * Called for every packet dropped while waiting for a fresh start, but sends at most one request
     * per interval. A request skipped because it fell inside the interval is therefore repeated by
     * the next dropped packet; asking only once left the picture frozen for good whenever that
     * single request was skipped, while input kept working.
     */
    private fun requestKeyFrameLocked() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastKeyFrameRequestAt < KEY_FRAME_REQUEST_INTERVAL_MS) return
        lastKeyFrameRequestAt = now
        onKeyFrameNeeded()
    }
}
