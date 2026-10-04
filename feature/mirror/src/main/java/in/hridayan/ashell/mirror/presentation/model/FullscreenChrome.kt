package `in`.hridayan.ashell.mirror.presentation.model

import `in`.hridayan.ashell.mirror.domain.model.MirrorState

/**
 * What the user asked of the fullscreen layout, kept apart from the session so a session update
 * never resets it and a layout change never touches the session.
 *
 * @property isRequested the user turned fullscreen on; it only takes effect while streaming.
 * @property isPanelOpen the floating control panel is showing.
 * @property isHeldForRestart the session is restarting because the user changed its quality, so
 * fullscreen stays on through the start-up states until video is back.
 */
data class FullscreenChrome(
    val isRequested: Boolean = false,
    val isPanelOpen: Boolean = false,
    val isHeldForRestart: Boolean = false
) {
    /**
     * Fullscreen is dropped as soon as the session stops streaming, so dialogs and progress always
     * appear in the normal layout, and a later reconnect or retry starts windowed rather than
     * jumping back into fullscreen unasked. A restart the user asked for is the one exception.
     */
    fun reconciledWith(session: MirrorState): FullscreenChrome = when {
        session is MirrorState.Streaming && session.videoSize != null -> copy(isHeldForRestart = false)
        session is MirrorState.Streaming -> this
        isHeldForRestart && (session is MirrorState.Idle || session is MirrorState.Starting) -> this
        else -> FullscreenChrome()
    }
}
