package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.mirror.domain.model.StreamStats

private const val MIN_ACTIVE_FRAMES = 10
private const val DROP_RATIO = 0.1
private const val SUSTAINED_SAMPLES = 5

/**
 * Decides when to offer a lower quality: dropped frames stayed above 10 % for five seconds while the
 * screen was changing. A still screen sends a frame only every 100 ms or so, which says nothing
 * about how well this phone keeps up. One detector belongs to one session.
 */
class StruggleDetector {

    private var strugglingSamples = 0
    private var hasFired = false

    /** @return true exactly once, at the sample that completes the first sustained struggle. */
    fun onSample(stats: StreamStats): Boolean {
        val total = stats.framesRendered + stats.framesDropped
        val isStruggling = total >= MIN_ACTIVE_FRAMES && stats.framesDropped > total * DROP_RATIO
        strugglingSamples = if (isStruggling) strugglingSamples + 1 else 0

        if (hasFired || strugglingSamples < SUSTAINED_SAMPLES) return false
        hasFired = true
        return true
    }
}
