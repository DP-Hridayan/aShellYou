package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.mirror.domain.model.VideoSize

private const val ALIGNMENT_SLACK = 16

/**
 * Recognises a server that had to downsize. When the device's encoder fails before its first frame,
 * the server retries at 2560, 1920, 1600 and so on, which costs seconds at every start. Starting the
 * next session at the size that worked skips that.
 */
object EncoderFallback {

    /**
     * Only the first capture session of a stream may be passed in: a later, smaller one can just be
     * a foldable folding.
     *
     * @return the long edge the encoder fell back to, or null when the stream is the size asked for.
     */
    fun detect(requestedMaxSize: Int, targetScreen: VideoSize?, streamed: VideoSize): Int? {
        val expected = expectedLongEdge(requestedMaxSize, targetScreen) ?: return null
        return streamed.longEdge.takeIf { it + ALIGNMENT_SLACK < expected }
    }

    private fun expectedLongEdge(requestedMaxSize: Int, targetScreen: VideoSize?): Int? {
        val native = targetScreen?.longEdge
        return when {
            requestedMaxSize <= 0 -> native
            native == null -> requestedMaxSize
            else -> minOf(requestedMaxSize, native)
        }
    }
}
