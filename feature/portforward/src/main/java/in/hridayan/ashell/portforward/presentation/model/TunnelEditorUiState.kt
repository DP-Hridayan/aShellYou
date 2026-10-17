package `in`.hridayan.ashell.portforward.presentation.model

import androidx.compose.runtime.Immutable
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.model.RecentHost
import `in`.hridayan.ashell.portforward.domain.model.TestResult
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.allowsNetworkSharing
import `in`.hridayan.ashell.portforward.domain.usecase.ValidateTunnelFormUseCase

/** The device a device tunnel reaches, as it was when the tunnel was made. */
@Immutable
data class EditorDevice(val transport: ExternalDeviceTransport, val deviceId: String?, val deviceName: String)

/** Which kind of tunnel the editor builds. */
sealed interface EditorKind {
    data object Network : EditorKind
    data class Device(val device: EditorDevice) : EditorKind
}

/**
 * The editable fields of a tunnel, kept as typed so that half-typed values survive.
 *
 * @property address the URL or address field, which fills [host], [targetPort] and [path] when it
 * parses. Network tunnels only.
 * @property endpointValue the device port, socket name, socket path or process id. Device tunnels
 * only.
 * @property sameAsTarget the local port follows the target port.
 */
@Immutable
data class TunnelEditorForm(
    val kind: EditorKind = EditorKind.Network,
    val endpointType: DeviceEndpointType = DeviceEndpointType.TCP,
    val endpointValue: String = "",
    val address: String = "",
    val host: String = "",
    val targetPort: String = "",
    val path: String? = null,
    val localPort: String = "",
    val sameAsTarget: Boolean = true,
    val name: String = "",
    val autoStart: Boolean = false,
    val shareOnNetwork: Boolean = false,
    val looksLikeHttp: Boolean = false
)

/** Whether another app holds the chosen local port. */
sealed interface LocalPortStatus {
    data object Unknown : LocalPortStatus
    data object Checking : LocalPortStatus
    data object Free : LocalPortStatus
    data class InUse(val suggestedPort: Int?) : LocalPortStatus
}

sealed interface TestState {
    data object Idle : TestState
    data object Testing : TestState
    data class Done(val result: TestResult) : TestState
}

/**
 * @property showErrors validation errors are shown only after a save or test attempt.
 * @property targetIsLocal the target resolves to the local network, which needs Android's local
 * network permission.
 * @property isRunning the tunnel being edited is running, so saving restarts it.
 * @property startError why the last start failed, shown until the form changes.
 * @property deviceConnected the device of a device tunnel is connected right now.
 */
@Immutable
data class TunnelEditorUiState(
    val form: TunnelEditorForm = TunnelEditorForm(),
    val validation: ValidateTunnelFormUseCase.Result = ValidateTunnelFormUseCase.Result(null, null, null),
    val showErrors: Boolean = false,
    val localPortStatus: LocalPortStatus = LocalPortStatus.Unknown,
    val test: TestState = TestState.Idle,
    val targetIsLocal: Boolean = false,
    val isEditing: Boolean = false,
    val isRunning: Boolean = false,
    val isSaving: Boolean = false,
    val lanSharingAllowed: Boolean = false,
    val recentHosts: List<RecentHost> = emptyList(),
    val startError: TunnelError? = null,
    val deviceConnected: Boolean = false
) {
    val isDevice: Boolean
        get() = form.kind is EditorKind.Device

    val canShareOnNetwork: Boolean
        get() = lanSharingAllowed && form.targetOrNull()?.allowsNetworkSharing != false

    val previewSpec: TunnelSpec?
        get() = form.toSpecOrNull(id = PREVIEW_ID)

    private companion object {
        const val PREVIEW_ID = "preview"
    }
}

sealed interface TunnelEditorEvent {
    data class Saved(val spec: TunnelSpec, val started: Boolean) : TunnelEditorEvent
    data object SaveFailed : TunnelEditorEvent

    /** An action was tapped while a field it needs has an error. */
    data object InvalidForm : TunnelEditorEvent
}
