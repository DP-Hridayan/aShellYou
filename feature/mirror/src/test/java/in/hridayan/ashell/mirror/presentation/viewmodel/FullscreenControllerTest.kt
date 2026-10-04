package `in`.hridayan.ashell.mirror.presentation.viewmodel

import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.InputAction
import `in`.hridayan.ashell.mirror.domain.model.MirrorState
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import `in`.hridayan.ashell.mirror.presentation.model.FullscreenChrome
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FullscreenControllerTest {

    private val repository = FakeMirrorRepository()
    private val settings = RecordingSettings()
    private val streaming = MirrorState.Streaming("Pixel", VideoSize(1080, 2400), isControlAvailable = true)

    private fun TestScope.controller() = FullscreenController(repository, settings, this)

    @Test
    fun `entering again keeps an open panel`() = runTest {
        val controller = controller()
        controller.onEnterFullscreen()
        controller.onToggleControlPanel()
        controller.onEnterFullscreen()

        assertEquals(FullscreenChrome(isRequested = true, isPanelOpen = true), controller.chrome.value)
    }

    @Test
    fun `exit and dismiss close the panel`() = runTest {
        val controller = controller()
        controller.onEnterFullscreen()
        controller.onToggleControlPanel()
        controller.onDismissControlPanel()
        assertEquals(FullscreenChrome(isRequested = true), controller.chrome.value)

        controller.onToggleControlPanel()
        controller.onExitFullscreen()
        assertEquals(FullscreenChrome(), controller.chrome.value)
    }

    @Test
    fun `a session that stops streaming drops fullscreen`() = runTest {
        val controller = controller()
        controller.onSession(streaming)
        controller.onEnterFullscreen()
        controller.onSession(streaming.copy(deviceName = "Pixel 9"))
        assertEquals(FullscreenChrome(isRequested = true), controller.chrome.value)

        controller.onSession(MirrorState.Reconnecting)
        assertEquals(FullscreenChrome(), controller.chrome.value)
    }

    @Test
    fun `holding for a restart closes the panel and only applies in fullscreen`() = runTest {
        val controller = controller()
        controller.holdThroughRestart()
        assertEquals(FullscreenChrome(), controller.chrome.value)

        controller.onEnterFullscreen()
        controller.onToggleControlPanel()
        controller.holdThroughRestart()
        assertEquals(FullscreenChrome(isRequested = true, isHeldForRestart = true), controller.chrome.value)
    }

    @Test
    fun `host back is one press of back or screen on`() = runTest {
        controller().onHostBack()

        assertEquals(
            listOf(
                ControlMessage.BackOrScreenOn(InputAction.DOWN),
                ControlMessage.BackOrScreenOn(InputAction.UP)
            ),
            repository.sent
        )
    }

    @Test
    fun `the hint is remembered once shown`() = runTest {
        controller().onFullscreenHintShown()
        advanceUntilIdle()

        assertEquals(mapOf(SettingsKeys.MirrorFullscreenHintShown to true), settings.booleans)
    }

    private class RecordingSettings : SettingsRepository {
        val booleans = mutableMapOf<SettingsKeys<Boolean>, Boolean>()
        override suspend fun setBoolean(key: SettingsKeys<Boolean>, value: Boolean) {
            booleans[key] = value
        }

        override val preferences get() = unused()
        override fun getBoolean(key: SettingsKeys<Boolean>) = unused()
        override suspend fun toggleSetting(key: SettingsKeys<Boolean>) = unused()
        override fun getInt(key: SettingsKeys<Int>) = unused()
        override suspend fun setInt(key: SettingsKeys<Int>, value: Int) = unused()
        override fun getFloat(key: SettingsKeys<Float>) = unused()
        override suspend fun setFloat(key: SettingsKeys<Float>, value: Float) = unused()
        override fun getString(key: SettingsKeys<String>) = unused()
        override suspend fun setString(key: SettingsKeys<String>, value: String) = unused()
        override fun getAllDefaultSettings() = unused()
        override fun getPreserveKeys() = unused()
        override suspend fun getCurrentSettings() = unused()
        override suspend fun resetAndRestoreDefaults() = unused()

        private fun unused(): Nothing = error("not used by the controller")
    }
}
