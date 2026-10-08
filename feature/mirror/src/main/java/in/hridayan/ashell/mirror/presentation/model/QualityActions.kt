package `in`.hridayan.ashell.mirror.presentation.model

import androidx.compose.runtime.Stable
import `in`.hridayan.ashell.mirror.domain.quality.QualitySelection
import `in`.hridayan.ashell.mirror.domain.quality.ResolvedQuality

@Stable
interface QualityActions {

    /** Saves [selection] for this transport and restarts the stream with it, unless nothing changed. */
    fun onApplyQuality(selection: QualitySelection)

    fun onStatsOverlayChange(isOn: Boolean)

    /** Applies the next preset below the current quality, from the can't-keep-up hint. */
    fun onLowerQuality()

    fun onStruggleHintShown()

    fun onCodecFallbackShown()

    /** What [selection] would stream at on this device, or null before the first session starts. */
    fun previewQuality(selection: QualitySelection): ResolvedQuality?
}
