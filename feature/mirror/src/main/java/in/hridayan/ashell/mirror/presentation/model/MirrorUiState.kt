package `in`.hridayan.ashell.mirror.presentation.model

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.domain.model.MirrorState

data class MirrorUiState(
    val transport: ExternalDeviceTransport,
    val session: MirrorState = MirrorState.Idle,
    val chrome: FullscreenChrome = FullscreenChrome()
) {
    /** Fullscreen only ever applies while streaming; every other state shows the normal layout. */
    val isFullscreen: Boolean
        get() = chrome.isRequested && (session as? MirrorState.Streaming)?.videoSize != null

    /**
     * Whether this phone's Back goes to the device rather than to the app. Only in fullscreen with
     * the panel closed, and never when the device refused input or a touch-exploration service such
     * as TalkBack is on: Back is then the user's only reliable way out of fullscreen.
     */
    fun backGoesToDevice(isTouchExplorationOn: Boolean): Boolean =
        isFullscreen &&
            !chrome.isPanelOpen &&
            !isTouchExplorationOn &&
            (session as? MirrorState.Streaming)?.isControlAvailable == true
}
