package `in`.hridayan.ashell.portforward.presentation.model

import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.model.DevicePreset
import `in`.hridayan.ashell.portforward.domain.model.RecentHost

/** Every change the tunnel editor can make, hoisted to its screen. */
data class TunnelEditorActions(
    val onPaste: () -> Unit,
    val onEndpointTypeChange: (DeviceEndpointType) -> Unit,
    val onPreset: (DevicePreset) -> Unit,
    val onSameAsTargetChange: (Boolean) -> Unit,
    val onAutoStartChange: (Boolean) -> Unit,
    val onShareOnNetworkChange: (Boolean) -> Unit,
    val onRecentHost: (RecentHost) -> Unit,
    val onUseSuggestedPort: (Int) -> Unit,
    val onRequestLocalNetwork: () -> Unit,
    val onOpenPermissionSettings: () -> Unit,
    val onTest: () -> Unit,
    val onSave: () -> Unit,
    val onStart: () -> Unit,
    val onCancel: () -> Unit
)
