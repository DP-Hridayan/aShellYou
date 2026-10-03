package `in`.hridayan.ashell.mirror.presentation.model

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.domain.model.MirrorState

data class MirrorUiState(
    val transport: ExternalDeviceTransport,
    val session: MirrorState = MirrorState.Idle
)
