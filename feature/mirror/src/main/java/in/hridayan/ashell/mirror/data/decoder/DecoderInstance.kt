package `in`.hridayan.ashell.mirror.data.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import `in`.hridayan.ashell.mirror.domain.model.MediaPacket
import `in`.hridayan.ashell.mirror.domain.model.VideoSize

private const val DECODER_THREAD_NAME = "MirrorDecoder"
private const val MAX_PENDING_PACKETS = 3
private const val REALTIME_PRIORITY = 0
private const val LOW_LATENCY_ON = 1
private const val YUV420_NUMERATOR = 3
private const val YUV420_DENOMINATOR = 2

/**
 * One configured `MediaCodec` decoding a single capture session straight onto a surface.
 *
 * It runs in asynchronous mode on its own thread. Packets wait for free input buffers in a short
 * queue; when the queue is full, a non-key frame is refused instead of queued. Video falls behind
 * rather than building latency, and the caller recovers with a fresh key frame.
 */
internal class DecoderInstance(
    mimeType: String,
    size: VideoSize,
    surface: Surface,
    private val onError: (Throwable) -> Unit,
    private val onFrameRendered: () -> Unit
) {

    private val thread = HandlerThread(DECODER_THREAD_NAME).apply { start() }
    private val codec = MediaCodec.createDecoderByType(mimeType)
    private val lock = Any()
    private val freeInputs = ArrayDeque<Int>()
    private val pending = ArrayDeque<MediaPacket>()

    @Volatile
    private var released = false

    init {
        codec.setCallback(Callbacks(), Handler(thread.looper))
        codec.configure(formatFor(mimeType, size), surface, null, 0)
        codec.start()
    }

    /** @return false when the packet was refused because the decoder is behind or it does not fit. */
    fun queue(packet: MediaPacket): Boolean = synchronized(lock) {
        val isDroppable = !packet.isConfig && !packet.isKeyFrame
        if (isDroppable && pending.size >= MAX_PENDING_PACKETS) return false
        pending.addLast(packet)
        drainLocked()
    }

    fun release() {
        synchronized(lock) {
            released = true
            pending.clear()
            freeInputs.clear()
        }
        runCatching { codec.stop() }
        runCatching { codec.release() }
        thread.quitSafely()
    }

    /** @return false when a packet had to be discarded because it exceeded the input buffer. */
    private fun drainLocked(): Boolean {
        var allFit = true
        while (!released && freeInputs.isNotEmpty() && pending.isNotEmpty()) {
            allFit = queueInput(freeInputs.removeFirst(), pending.removeFirst()) && allFit
        }
        return allFit
    }

    /** An oversized packet still returns its buffer to the codec, empty, so the buffer is not lost. */
    private fun queueInput(index: Int, packet: MediaPacket): Boolean {
        val buffer = codec.getInputBuffer(index) ?: return true
        val fits = packet.data.size <= buffer.capacity()
        if (fits) {
            buffer.clear()
            buffer.put(packet.data)
        }
        val size = if (fits) packet.data.size else 0
        val flags = if (fits) flagsFor(packet) else 0
        codec.queueInputBuffer(index, 0, size, packet.ptsMicros, flags)
        return fits
    }

    private fun flagsFor(packet: MediaPacket): Int = when {
        packet.isConfig -> MediaCodec.BUFFER_FLAG_CODEC_CONFIG
        packet.isKeyFrame -> MediaCodec.BUFFER_FLAG_KEY_FRAME
        else -> 0
    }

    private fun formatFor(mimeType: String, size: VideoSize): MediaFormat =
        MediaFormat.createVideoFormat(mimeType, size.width, size.height).apply {
            setInteger(
                MediaFormat.KEY_MAX_INPUT_SIZE,
                size.width * size.height * YUV420_NUMERATOR / YUV420_DENOMINATOR
            )
            setInteger(MediaFormat.KEY_PRIORITY, REALTIME_PRIORITY)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setInteger(MediaFormat.KEY_LOW_LATENCY, LOW_LATENCY_ON)
            }
        }

    private inner class Callbacks : MediaCodec.Callback() {

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            synchronized(lock) {
                if (released) return
                freeInputs.addLast(index)
                drainLocked()
            }
        }

        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            if (released) return
            runCatching { codec.releaseOutputBuffer(index, true) }.onSuccess { onFrameRendered() }
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            if (!released) onError(e)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) = Unit
    }
}
