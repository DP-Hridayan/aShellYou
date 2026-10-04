package `in`.hridayan.ashell.mirror.presentation.viewmodel

import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.InputAction
import `in`.hridayan.ashell.mirror.domain.model.MirrorState
import `in`.hridayan.ashell.mirror.domain.repository.MirrorRepository
import `in`.hridayan.ashell.mirror.presentation.model.FullscreenActions
import `in`.hridayan.ashell.mirror.presentation.model.FullscreenChrome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The fullscreen layout's state and actions for one mirror screen, owned by its ViewModel. */
class FullscreenController(
    private val repository: MirrorRepository,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope
) : FullscreenActions {

    private val _chrome = MutableStateFlow(FullscreenChrome())
    val chrome: StateFlow<FullscreenChrome> = _chrome.asStateFlow()

    fun onSession(session: MirrorState) = _chrome.update { it.reconciledWith(session) }

    override fun onEnterFullscreen() = _chrome.update {
        if (it.isRequested) it else FullscreenChrome(isRequested = true)
    }

    override fun onExitFullscreen() {
        _chrome.value = FullscreenChrome()
    }

    override fun onToggleControlPanel() = _chrome.update { it.copy(isPanelOpen = !it.isPanelOpen) }

    override fun onDismissControlPanel() = _chrome.update { it.copy(isPanelOpen = false) }

    /** Sent as back-or-screen-on, as desktop scrcpy does: Back while the screen is on, wake otherwise. */
    override fun onHostBack() {
        repository.send(ControlMessage.BackOrScreenOn(InputAction.DOWN))
        repository.send(ControlMessage.BackOrScreenOn(InputAction.UP))
    }

    override fun onFullscreenHintShown() {
        scope.launch { settingsRepository.setBoolean(SettingsKeys.MirrorFullscreenHintShown, true) }
    }
}
