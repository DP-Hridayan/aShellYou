package `in`.hridayan.ashell.mirror.presentation.viewmodel

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.MirrorOptions
import `in`.hridayan.ashell.mirror.domain.model.MirrorState
import `in`.hridayan.ashell.mirror.domain.model.StreamStats
import `in`.hridayan.ashell.mirror.domain.model.VideoOutput
import `in`.hridayan.ashell.mirror.domain.quality.QualitySelection
import `in`.hridayan.ashell.mirror.domain.quality.TargetDevice
import `in`.hridayan.ashell.mirror.domain.quality.ViewerProfile
import `in`.hridayan.ashell.mirror.domain.repository.MirrorRepository
import `in`.hridayan.ashell.mirror.domain.repository.QualityRepository
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeMirrorRepository : MirrorRepository {
    val sent = mutableListOf<ControlMessage>()

    override val state = MutableStateFlow<MirrorState>(MirrorState.Idle)
    override val stats = MutableStateFlow<StreamStats?>(null)

    override suspend fun run(transport: ExternalDeviceTransport, options: suspend (TargetDevice) -> MirrorOptions) =
        Unit

    override fun send(message: ControlMessage) {
        sent += message
    }

    override fun setVideoOutput(output: VideoOutput?) = Unit
}

internal class FakeQualityRepository(private val viewer: ViewerProfile) : QualityRepository {
    val selections = MutableStateFlow(QualitySelection())
    val limits = mutableMapOf<String, Int>()

    override fun selection(transport: ExternalDeviceTransport) = selections

    override suspend fun saveSelection(transport: ExternalDeviceTransport, selection: QualitySelection) {
        selections.value = selection
    }

    override val isStatsOverlayOn = MutableStateFlow(false)

    override suspend fun setStatsOverlayOn(isOn: Boolean) {
        isStatsOverlayOn.value = isOn
    }

    override fun encoderLimit(serial: String): Int? = limits[serial]

    override fun rememberEncoderLimit(serial: String, longEdge: Int) {
        limits[serial] = longEdge
    }

    override suspend fun viewerProfile(): ViewerProfile = viewer
}
