package `in`.hridayan.ashell.mirror.presentation.model

import `in`.hridayan.ashell.mirror.domain.model.MirrorState

/**
 * What the user asked of the fullscreen layout, kept apart from the session so a session update
 * never resets it and a layout change never touches the session.
 *
 * @property isRequested the user turned fullscreen on; it only takes effect while streaming.
 * @property isPanelOpen the floating control panel is showing.
 */
data class FullscreenChrome(
    val isRequested: Boolean = false,
    val isPanelOpen: Boolean = false
) {
    /**
     * Fullscreen is dropped as soon as the session stops streaming, so dialogs and progress always
     * appear in the normal layout, and a later reconnect or retry starts windowed rather than
     * jumping back into fullscreen unasked.
     */
    fun reconciledWith(session: MirrorState): FullscreenChrome =
        if (session is MirrorState.Streaming) this else FullscreenChrome()
}
