package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.mirror.domain.model.VideoSize

private const val EVEN = 2

/** The size the server's `max_size` produces, before the encoder's own alignment. */
object VideoScaling {

    /**
     * [screen] with its long edge limited to [maxLongEdge] and its aspect kept. A limit of 0, or one
     * at or above the screen's long edge, leaves the screen as it is, because the server never
     * upscales.
     */
    fun scaled(screen: VideoSize, maxLongEdge: Int): VideoSize {
        if (maxLongEdge <= 0 || maxLongEdge >= screen.longEdge) return screen
        val shortEdge = (screen.shortEdge.toLong() * maxLongEdge / screen.longEdge).toInt() / EVEN * EVEN
        return if (screen.isLandscape) VideoSize(maxLongEdge, shortEdge) else VideoSize(shortEdge, maxLongEdge)
    }
}
