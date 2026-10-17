package `in`.hridayan.ashell.portforward.presentation.model

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState

/** What a tunnel card can ask for, hoisted to the screen. */
data class TunnelCardActions(
    val onClick: () -> Unit,
    val onRunningChange: (Boolean) -> Unit,
    val onOpen: () -> Unit,
    val onCopy: () -> Unit,
    val onUsePort: (Int) -> Unit,
    val onEdit: () -> Unit,
    val onReconnect: () -> Unit,
    val onUseDevice: (ExternalDeviceState.Connected) -> Unit
)
