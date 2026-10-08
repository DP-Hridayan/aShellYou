package `in`.hridayan.ashell.mirror.domain.quality

import `in`.hridayan.ashell.mirror.domain.model.MirrorQualityPreset

/** The one-tap fix offered when the video can't keep up. */
object QualityStepDown {

    /** @return the next preset below [current], or null when it is already at the lowest. */
    fun lower(current: ResolvedQuality): QualityChoice.Preset? {
        val longEdge = current.expectedSize?.longEdge ?: current.options.maxSize
        val fps = current.options.maxFps
        val preset = listOf(MirrorQualityPreset.BALANCED, MirrorQualityPreset.SAVER)
            .firstOrNull { longEdge > it.maxSize || fps > it.maxFps }
        return preset?.let { QualityChoice.Preset(it) }
    }
}
