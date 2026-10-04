package `in`.hridayan.ashell.mirror.presentation.model

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.domain.model.EndReason
import `in`.hridayan.ashell.mirror.domain.model.MirrorError
import `in`.hridayan.ashell.mirror.domain.model.MirrorState
import `in`.hridayan.ashell.mirror.domain.model.StartStep
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenChromeTest {

    private val streaming = MirrorState.Streaming("Pixel", VideoSize(1080, 2400), isControlAvailable = true)
    private val requested = FullscreenChrome(isRequested = true)

    private fun state(session: MirrorState, chrome: FullscreenChrome = requested) =
        MirrorUiState(transport = ExternalDeviceTransport.OTG, session = session, chrome = chrome)

    @Test
    fun `fullscreen applies only while streaming with a known size`() {
        assertTrue(state(streaming).isFullscreen)
        assertFalse(state(streaming.copy(videoSize = null)).isFullscreen)
        assertFalse(state(MirrorState.Starting(StartStep.CONNECTING)).isFullscreen)
        assertFalse(state(MirrorState.Reconnecting).isFullscreen)
        assertFalse(state(MirrorState.Failed(MirrorError.StreamError)).isFullscreen)
        assertFalse(state(MirrorState.Ended(EndReason.SERVER_STOPPED)).isFullscreen)
        assertFalse(state(streaming, FullscreenChrome()).isFullscreen)
    }

    @Test
    fun `back goes to the device only in fullscreen with the panel closed and control available`() {
        assertTrue(state(streaming).backGoesToDevice(isTouchExplorationOn = false))
    }

    @Test
    fun `back closes an open panel instead of reaching the device`() {
        val open = state(streaming, requested.copy(isPanelOpen = true))

        assertFalse(open.backGoesToDevice(isTouchExplorationOn = false))
    }

    @Test
    fun `back stays with the app when the device refused input or touch exploration is on`() {
        val viewOnly = state(streaming.copy(isControlAvailable = false))

        assertFalse(viewOnly.backGoesToDevice(isTouchExplorationOn = false))
        assertFalse(state(streaming).backGoesToDevice(isTouchExplorationOn = true))
    }

    @Test
    fun `back is never taken over outside fullscreen`() {
        assertFalse(state(streaming, FullscreenChrome()).backGoesToDevice(isTouchExplorationOn = false))
    }

    @Test
    fun `leaving the streaming state clears fullscreen so a later session starts windowed`() {
        val open = requested.copy(isPanelOpen = true)

        assertEquals(FullscreenChrome(), open.reconciledWith(MirrorState.Reconnecting))
        assertEquals(FullscreenChrome(), open.reconciledWith(MirrorState.Failed(MirrorError.StreamError)))
        assertEquals(open, open.reconciledWith(streaming))
    }
}
