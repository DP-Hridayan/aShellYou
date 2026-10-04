package `in`.hridayan.ashell.mirror.data.decoder

import `in`.hridayan.ashell.mirror.domain.model.StreamStats
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

private const val BITS_PER_BYTE = 8

/**
 * Running totals for the stream statistics. The reader and decoder threads write them, and a
 * sampler drains them once a second, so each [StreamStats] covers exactly one interval.
 */
class StreamCounters {

    private val rendered = AtomicInteger()
    private val dropped = AtomicInteger()
    private val bytes = AtomicLong()

    fun onRendered() {
        rendered.incrementAndGet()
    }

    fun onDropped() {
        dropped.incrementAndGet()
    }

    fun onReceived(byteCount: Int) {
        bytes.addAndGet(byteCount.toLong())
    }

    fun drain(): StreamStats = StreamStats(
        framesRendered = rendered.getAndSet(0),
        framesDropped = dropped.getAndSet(0),
        bitsPerSecond = bytes.getAndSet(0) * BITS_PER_BYTE
    )
}
