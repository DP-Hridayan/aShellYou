package `in`.hridayan.ashell.mirror.domain.model

/**
 * One second of a stream, as measured on this phone.
 *
 * @property framesRendered frames the decoder put on screen.
 * @property framesDropped frames discarded because the decoder was behind or was waiting for a key
 * frame.
 * @property bitsPerSecond encoded video received.
 */
data class StreamStats(
    val framesRendered: Int,
    val framesDropped: Int,
    val bitsPerSecond: Long
)
