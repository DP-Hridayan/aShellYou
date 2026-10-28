package `in`.hridayan.ashell.portforward.presentation.components.editor

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.model.browserUrl
import `in`.hridayan.ashell.portforward.presentation.model.EditorKind
import `in`.hridayan.ashell.portforward.presentation.model.LocalPortStatus
import `in`.hridayan.ashell.portforward.presentation.model.TunnelEditorActions
import `in`.hridayan.ashell.portforward.presentation.model.TunnelEditorUiState
import `in`.hridayan.ashell.portforward.presentation.permission.LocalNetworkAccessState

private const val ROOT_PATH = "/"

/**
 * The fields of the tunnel editor, in the order a user fills them: where the target is, how it is
 * reached on this phone, options, and a test. The actions float above the list, so it ends with
 * room for them.
 */
@Composable
fun TunnelEditorContent(
    state: TunnelEditorUiState,
    localNetwork: LocalNetworkAccessState,
    actions: TunnelEditorActions,
    listState: LazyListState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val needsPermission = state.needsLocalNetworkPermission(localNetwork)
    val layoutDirection = LocalLayoutDirection.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        state = listState,
        contentPadding = PaddingValues(
            start = EditorSpacing.PageHorizontal + contentPadding.calculateStartPadding(layoutDirection),
            end = EditorSpacing.PageHorizontal + contentPadding.calculateEndPadding(layoutDirection),
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + EditorSpacing.BottomClearance
        )
    ) {
        when (val kind = state.form.kind) {
            EditorKind.Network -> networkTarget(state, actions)
            is EditorKind.Device -> deviceTarget(state, kind, actions)
        }
        if (needsPermission) permission(localNetwork, actions)
        localSection(state, actions)
        optionsSection(state, actions)
        checkSection(state, onTest = actions.onTest)
    }
}

/**
 * The index of the first list item that shows a field error, or null when there is none. It
 * follows the item order of [TunnelEditorContent], so the two change together.
 */
fun TunnelEditorUiState.firstErrorIndex(needsPermission: Boolean): Int? {
    val targetError = validation.host != null || validation.targetPort != null
    val targetItems = if (isDevice) DEVICE_TARGET_ITEMS else NETWORK_TARGET_ITEMS
    return when {
        targetError -> if (isDevice) DEVICE_SOCKET_LABEL_INDEX else 0
        validation.localPort != null -> targetItems + if (needsPermission) 1 else 0
        else -> null
    }
}

private const val NETWORK_TARGET_ITEMS = 4
private const val DEVICE_TARGET_ITEMS = 5
private const val DEVICE_SOCKET_LABEL_INDEX = 3

private fun LazyListScope.networkTarget(state: TunnelEditorUiState, actions: TunnelEditorActions) {
    item(key = "target_label") { EditorSectionLabel(stringResource(R.string.port_forward_section_target)) }
    item(key = "address") {
        AddressField(value = state.form.address, onValueChange = actions.onAddressChange, onPaste = actions.onPaste)
        EditorCaption(text = parsedSummary(state))
    }
    item(key = "recent") { RecentHostChips(hosts = state.recentHosts, onSelect = actions.onRecentHost) }
    item(key = "host_port") {
        val errors = state.validation.takeIf { state.showErrors }
        Spacer(modifier = Modifier.height(EditorSpacing.BlockGap))
        HostAndPortFields(
            host = state.form.host,
            port = state.form.targetPort,
            hostError = errors?.host,
            portError = errors?.targetPort,
            onHostChange = actions.onHostChange,
            onPortChange = actions.onTargetPortChange
        )
    }
}

private fun LazyListScope.deviceTarget(
    state: TunnelEditorUiState,
    kind: EditorKind.Device,
    actions: TunnelEditorActions
) {
    item(key = "device_label") { EditorSectionLabel(stringResource(R.string.port_forward_section_device)) }
    item(key = "device") { DeviceHeader(device = kind.device, connected = state.deviceConnected) }
    item(key = "presets") { PresetChips(onPreset = actions.onPreset) }
    item(key = "socket_label") { EditorSectionLabel(stringResource(R.string.port_forward_target_type)) }
    item(key = "socket") {
        val error = state.validation.targetPort?.takeIf { state.showErrors }
        EndpointTypeSelector(type = state.form.endpointType, onTypeChange = actions.onEndpointTypeChange)
        Spacer(modifier = Modifier.height(EditorSpacing.BlockGap))
        EndpointValueField(
            type = state.form.endpointType,
            value = state.form.endpointValue,
            error = error,
            position = GroupPosition.Single,
            onValueChange = actions.onEndpointValueChange
        )
        EditorCaption(text = error?.text(), isError = true)
    }
}

private fun LazyListScope.permission(localNetwork: LocalNetworkAccessState, actions: TunnelEditorActions) {
    item(key = "permission") {
        Spacer(modifier = Modifier.height(EditorSpacing.BlockGap))
        LocalNetworkPermissionCard(
            permanentlyDenied = localNetwork.permanentlyDenied,
            onAllow = actions.onRequestLocalNetwork,
            onOpenSettings = actions.onOpenPermissionSettings
        )
    }
}

private fun LazyListScope.localSection(state: TunnelEditorUiState, actions: TunnelEditorActions) {
    item(key = "local_label") { EditorSectionLabel(stringResource(R.string.port_forward_section_local)) }
    item(key = "local") {
        val error = state.validation.localPort?.takeIf { state.showErrors || state.form.localPort.isNotEmpty() }
        LocalGroup(state = state, isError = error != null, actions = actions)
        LocalCaption(state = state, error = error?.text(), suggestedPort = state.suggestedPort(), actions = actions)
    }
}

/** The local port, then the switches that apply to this tunnel, as one group. */
@Composable
private fun LocalGroup(state: TunnelEditorUiState, isError: Boolean, actions: TunnelEditorActions) {
    val form = state.form
    val showSameAsTarget = !state.isDevice || form.endpointType == DeviceEndpointType.TCP
    val rows = 1 + (if (showSameAsTarget) 1 else 0) + (if (state.canShareOnNetwork) 1 else 0)
    EditorGroup {
        LocalPortField(
            value = form.localPort,
            status = state.localPortStatus,
            isError = isError,
            position = GroupPosition.of(0, rows),
            onValueChange = actions.onLocalPortChange
        )
        if (showSameAsTarget) {
            EditorSwitchRow(
                title = stringResource(R.string.port_forward_same_as_target),
                checked = form.sameAsTarget,
                onCheckedChange = actions.onSameAsTargetChange,
                position = GroupPosition.of(1, rows)
            )
        }
        if (state.canShareOnNetwork) {
            EditorSwitchRow(
                title = stringResource(R.string.port_forward_share_on_network),
                description = stringResource(R.string.port_forward_share_on_network_description),
                checked = form.shareOnNetwork,
                onCheckedChange = actions.onShareOnNetworkChange,
                position = GroupPosition.of(rows - 1, rows)
            )
        }
    }
}

@Composable
private fun LocalCaption(
    state: TunnelEditorUiState,
    error: String?,
    suggestedPort: Int?,
    actions: TunnelEditorActions
) {
    when {
        error != null -> EditorCaption(text = error, isError = true)
        suggestedPort != null -> SuggestedPortChip(port = suggestedPort, onUse = actions.onUseSuggestedPort)
        else -> EditorCaption(
            text = state.previewSpec?.let { stringResource(R.string.port_forward_opens_at, it.browserUrl) }
        )
    }
}

private fun LazyListScope.optionsSection(state: TunnelEditorUiState, actions: TunnelEditorActions) {
    item(key = "options_label") { EditorSectionLabel(stringResource(R.string.port_forward_section_options)) }
    item(key = "options") {
        val autoStartTitle = if (state.isDevice) {
            R.string.port_forward_start_on_device_connect
        } else {
            R.string.port_forward_start_on_app_open
        }
        EditorGroup {
            EditorTextField(
                value = state.form.name,
                onValueChange = actions.onNameChange,
                label = stringResource(R.string.port_forward_name_optional),
                position = GroupPosition.First,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            EditorSwitchRow(
                title = stringResource(autoStartTitle),
                checked = state.form.autoStart,
                onCheckedChange = actions.onAutoStartChange,
                position = GroupPosition.Last
            )
        }
    }
}

private fun LazyListScope.checkSection(state: TunnelEditorUiState, onTest: () -> Unit) {
    item(key = "check_label") { EditorSectionLabel(stringResource(R.string.port_forward_section_check)) }
    item(key = "test") { TestCard(state = state.test, spec = state.previewSpec, onTest = onTest) }
    val error = state.startError
    val spec = state.previewSpec
    if (error != null && spec != null) {
        item(key = "start_error") {
            Spacer(modifier = Modifier.height(EditorSpacing.BlockGap))
            StartErrorCard(error = error, spec = spec)
        }
    }
}

/** True while Android's local network permission blocks the tunnel being edited. */
fun TunnelEditorUiState.needsLocalNetworkPermission(localNetwork: LocalNetworkAccessState): Boolean =
    !localNetwork.granted && (targetIsLocal || form.shareOnNetwork)

private fun TunnelEditorUiState.suggestedPort(): Int? = (localPortStatus as? LocalPortStatus.InUse)?.suggestedPort

@Composable
private fun parsedSummary(state: TunnelEditorUiState): String? {
    val form = state.form
    if (form.address.isBlank() || form.host.isBlank()) return null
    return stringResource(R.string.port_forward_parsed_address, form.host, form.targetPort, form.path ?: ROOT_PATH)
}
