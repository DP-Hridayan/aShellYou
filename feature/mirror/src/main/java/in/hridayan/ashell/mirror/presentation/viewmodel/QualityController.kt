package `in`.hridayan.ashell.mirror.presentation.viewmodel

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.domain.model.MirrorError
import `in`.hridayan.ashell.mirror.domain.model.MirrorOptions
import `in`.hridayan.ashell.mirror.domain.model.MirrorState
import `in`.hridayan.ashell.mirror.domain.model.StreamStats
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import `in`.hridayan.ashell.mirror.domain.model.VideoSize
import `in`.hridayan.ashell.mirror.domain.quality.EncoderFallback
import `in`.hridayan.ashell.mirror.domain.quality.QualityRequest
import `in`.hridayan.ashell.mirror.domain.quality.QualityResolver
import `in`.hridayan.ashell.mirror.domain.quality.QualitySelection
import `in`.hridayan.ashell.mirror.domain.quality.QualityStepDown
import `in`.hridayan.ashell.mirror.domain.quality.ResolvedQuality
import `in`.hridayan.ashell.mirror.domain.quality.StruggleDetector
import `in`.hridayan.ashell.mirror.domain.quality.TargetDevice
import `in`.hridayan.ashell.mirror.domain.quality.ViewerProfile
import `in`.hridayan.ashell.mirror.domain.repository.MirrorRepository
import `in`.hridayan.ashell.mirror.domain.repository.QualityRepository
import `in`.hridayan.ashell.mirror.presentation.model.QualityActions
import `in`.hridayan.ashell.mirror.presentation.model.QualityUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Chooses each session's video quality and reacts to how the stream does, for one mirror screen.
 *
 * The device reports its screen while each session is prepared. When a report comes back empty,
 * the last one from this screen is used, since a device's screen doesn't change between restarts. A codec the device fails to encode is swapped for
 * H.264 for the rest of the screen, without changing what is saved.
 *
 * @param restartSession starts a new session, which asks [optionsForNextSession] again.
 */
class QualityController(
    private val transport: ExternalDeviceTransport,
    private val repository: MirrorRepository,
    private val qualityRepository: QualityRepository,
    private val resolver: QualityResolver,
    private val scope: CoroutineScope,
    private val restartSession: () -> Unit
) : QualityActions {

    private val _state = MutableStateFlow(QualityUiState())
    val state: StateFlow<QualityUiState> = _state.asStateFlow()

    private var target: TargetDevice? = null
    private var viewer: ViewerProfile? = null
    private var isCodecFallenBack = false
    private var session = SessionWatch()

    init {
        scope.launch { qualityRepository.selection(transport).collect(::onSelection) }
        scope.launch { qualityRepository.isStatsOverlayOn.collect(::onStatsOverlay) }
        scope.launch { repository.stats.collect(::onStats) }
    }

    suspend fun optionsForNextSession(reported: TargetDevice): MirrorOptions {
        val viewerProfile = viewer ?: qualityRepository.viewerProfile().also { viewer = it }
        val device = reported.takeIf { it != TargetDevice.UNKNOWN }?.also { target = it }
            ?: target
            ?: TargetDevice.UNKNOWN
        val selection = qualityRepository.selection(transport).first()
        val resolved = resolve(selection, device, viewerProfile)

        session = SessionWatch()
        _state.update {
            it.copy(
                resolved = resolved,
                viewerRefreshRate = viewerProfile.refreshRate,
                supportedCodecs = viewerProfile.supportedCodecs
            )
        }
        return resolved.options
    }

    fun onSession(state: MirrorState) {
        when (state) {
            is MirrorState.Streaming -> state.videoSize?.let(::rememberEncoderFallback)
            is MirrorState.Failed -> if (state.error is MirrorError.EncoderFailed) fallBackToH264()
            else -> Unit
        }
    }

    override fun onApplyQuality(selection: QualitySelection) {
        if (selection == _state.value.selection) return
        scope.launch {
            qualityRepository.saveSelection(transport, selection)
            isCodecFallenBack = false
            restartSession()
        }
    }

    override fun onStatsOverlayChange(isOn: Boolean) {
        scope.launch { qualityRepository.setStatsOverlayOn(isOn) }
    }

    override fun onLowerQuality() {
        val current = _state.value
        val lower = current.resolved?.let(QualityStepDown::lower) ?: return
        onApplyQuality(current.selection.withPreset(lower.preset))
    }

    override fun onStruggleHintShown() = _state.update { it.copy(isStruggleHintPending = false) }

    override fun onCodecFallbackShown() = _state.update { it.copy(codecFallback = null) }

    override fun previewQuality(selection: QualitySelection): ResolvedQuality? {
        val viewerProfile = viewer ?: return null
        return resolve(selection, target ?: TargetDevice.UNKNOWN, viewerProfile)
    }

    /**
     * A codec this phone can't decode, say from a backup made on another phone, is quietly replaced
     * too, because the stream would fail on this side.
     */
    private fun resolve(
        selection: QualitySelection,
        device: TargetDevice,
        viewerProfile: ViewerProfile
    ): ResolvedQuality {
        val isUsable = !isCodecFallenBack && selection.custom.codec in viewerProfile.supportedCodecs
        val custom = if (isUsable) selection.custom else selection.custom.copy(codec = VideoCodec.H264)
        val choice = selection.copy(custom = custom).choice
        val encoderLimit = device.serial?.let(qualityRepository::encoderLimit)
        return resolver.resolve(QualityRequest(choice, transport, device, viewerProfile, encoderLimit))
    }

    /** Only the first video of a session is checked; see [EncoderFallback.detect]. */
    private fun rememberEncoderFallback(streamed: VideoSize) {
        if (session.hasCheckedEncoder) return
        session.hasCheckedEncoder = true
        val device = target ?: return
        val serial = device.serial ?: return
        val requested = _state.value.resolved?.options?.maxSize ?: return
        EncoderFallback.detect(
            requested,
            device.screen,
            streamed
        )?.let { qualityRepository.rememberEncoderLimit(serial, it) }
    }

    private fun fallBackToH264() {
        val failedCodec = _state.value.resolved?.options?.videoCodec ?: return
        if (failedCodec == VideoCodec.H264 || isCodecFallenBack) return
        isCodecFallenBack = true
        _state.update { it.copy(codecFallback = failedCodec) }
        restartSession()
    }

    private fun onSelection(selection: QualitySelection) = _state.update { it.copy(selection = selection) }

    private fun onStatsOverlay(isOn: Boolean) = _state.update { it.copy(isStatsOverlayOn = isOn) }

    private fun onStats(stats: StreamStats?) {
        _state.update { it.copy(stats = stats) }
        if (stats == null || !session.struggle.onSample(stats)) return
        if (_state.value.resolved?.let(QualityStepDown::lower) != null) {
            _state.update { it.copy(isStruggleHintPending = true) }
        }
    }

    /** What is tracked for one session only. */
    private class SessionWatch {
        val struggle = StruggleDetector()
        var hasCheckedEncoder = false
    }
}
