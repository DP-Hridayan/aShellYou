package `in`.hridayan.ashell.mirror.presentation.viewmodel

import android.util.Log
import android.view.Surface
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.mirror.data.decoder.SurfaceVideoOutput
import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.DeviceKey
import `in`.hridayan.ashell.mirror.domain.quality.QualityResolver
import `in`.hridayan.ashell.mirror.domain.repository.MirrorRepository
import `in`.hridayan.ashell.mirror.domain.repository.QualityRepository
import `in`.hridayan.ashell.mirror.presentation.model.MirrorActions
import `in`.hridayan.ashell.mirror.presentation.model.MirrorUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STATE_SHARING_TIMEOUT_MS = 5_000L
private const val TAG = "MirrorSession"

/**
 * Owns the mirroring session for one mirror screen. The session stops when the app goes to the
 * background, because a hidden mirror would keep the other device encoding for nobody, and starts
 * again when it returns; with the server already on the device that takes about a second.
 */
@HiltViewModel
class MirrorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MirrorRepository,
    settingsRepository: SettingsRepository,
    qualityRepository: QualityRepository,
    qualityResolver: QualityResolver
) : ViewModel(), MirrorActions {

    private val transport = savedStateHandle.toRoute<NavRoutes.MirrorScreen>().transport

    val fullscreen = FullscreenController(repository, settingsRepository, viewModelScope)

    val quality = QualityController(
        transport = transport,
        repository = repository,
        qualityRepository = qualityRepository,
        resolver = qualityResolver,
        scope = viewModelScope,
        restartSession = ::restartKeepingFullscreen
    )

    val uiState: StateFlow<MirrorUiState> = combine(
        repository.state.onEach { Log.i(TAG, "$transport session: $it") },
        fullscreen.chrome,
        quality.state
    ) { session, chrome, quality ->
        MirrorUiState(
            transport = transport,
            session = session,
            chrome = chrome.reconciledWith(session),
            quality = quality
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
            initialValue = MirrorUiState(transport = transport)
        )

    private var session: Job? = null
    private var stoppedInBackground = false

    init {
        startSession()
        viewModelScope.launch {
            repository.state.collect { session ->
                fullscreen.onSession(session)
                quality.onSession(session)
            }
        }
    }

    fun onStop(isChangingConfigurations: Boolean) {
        if (isChangingConfigurations) return
        session?.cancel()
        stoppedInBackground = true
    }

    fun onStart() {
        if (!stoppedInBackground) return
        stoppedInBackground = false
        startSession()
    }

    override fun onSurfaceAvailable(surface: Surface) =
        repository.setVideoOutput(SurfaceVideoOutput(surface))

    override fun onSurfaceDestroyed() = repository.setVideoOutput(null)

    override fun onTouch(messages: List<ControlMessage>) = messages.forEach(repository::send)

    override fun onDeviceKey(key: DeviceKey) = key.pressMessages().forEach(repository::send)

    override fun onExpandQuickSettings() = repository.send(ControlMessage.ExpandSettingsPanel)

    override fun onRetry() = startSession()

    /**
     * The previous session is fully torn down before the next one starts. Both run on the same
     * repository and the same device, so overlapping them let the old session's cleanup stop the new
     * session's server.
     */
    private fun startSession() {
        val previous = session
        session = viewModelScope.launch {
            previous?.cancelAndJoin()
            repository.run(transport, quality::optionsForNextSession)
        }
    }

    private fun restartKeepingFullscreen() {
        fullscreen.holdThroughRestart()
        startSession()
    }
}
