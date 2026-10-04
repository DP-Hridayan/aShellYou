package `in`.hridayan.ashell.mirror.presentation.model

import androidx.compose.runtime.Stable

@Stable
interface FullscreenActions {
    fun onEnterFullscreen()
    fun onExitFullscreen()
    fun onToggleControlPanel()
    fun onDismissControlPanel()

    /** This phone's Back, forwarded to the device while fullscreen. */
    fun onHostBack()
    fun onFullscreenHintShown()
}
