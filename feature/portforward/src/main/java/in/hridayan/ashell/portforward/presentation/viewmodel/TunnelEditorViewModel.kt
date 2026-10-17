package `in`.hridayan.ashell.portforward.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.model.DevicePreset
import `in`.hridayan.ashell.portforward.domain.model.ParsedHostInput
import `in`.hridayan.ashell.portforward.domain.model.RecentHost
import `in`.hridayan.ashell.portforward.domain.model.TestResult
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.isRunning
import `in`.hridayan.ashell.portforward.domain.model.toTunnelError
import `in`.hridayan.ashell.portforward.domain.repository.ConnectedDevices
import `in`.hridayan.ashell.portforward.domain.repository.LocalNetworkGate
import `in`.hridayan.ashell.portforward.domain.repository.PortAvailability
import `in`.hridayan.ashell.portforward.domain.repository.SavedTunnelRepository
import `in`.hridayan.ashell.portforward.domain.repository.TunnelTester
import `in`.hridayan.ashell.portforward.domain.usecase.ParseHostInputUseCase
import `in`.hridayan.ashell.portforward.domain.usecase.ValidateTunnelFormUseCase
import `in`.hridayan.ashell.portforward.presentation.model.EditorDevice
import `in`.hridayan.ashell.portforward.presentation.model.EditorKind
import `in`.hridayan.ashell.portforward.presentation.model.LocalPortStatus
import `in`.hridayan.ashell.portforward.presentation.model.TestState
import `in`.hridayan.ashell.portforward.presentation.model.TunnelEditorEvent
import `in`.hridayan.ashell.portforward.presentation.model.TunnelEditorForm
import `in`.hridayan.ashell.portforward.presentation.model.TunnelEditorUiState
import `in`.hridayan.ashell.portforward.presentation.model.targetOrNull
import `in`.hridayan.ashell.portforward.presentation.model.toForm
import `in`.hridayan.ashell.portforward.presentation.model.toSpecOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Backs the screen that creates or edits one tunnel.
 *
 * Checks that need the network or a trial bind run shortly after typing stops, so they do not run
 * on every keystroke.
 */
@HiltViewModel
class TunnelEditorViewModel internal constructor(
    private val tunnelId: String?,
    private val transport: ExternalDeviceTransport?,
    private val devices: ConnectedDevices,
    private val repository: SavedTunnelRepository,
    private val controller: TunnelController,
    private val tester: TunnelTester,
    private val ports: PortAvailability,
    private val localNetwork: LocalNetworkGate,
    private val settings: SettingsRepository,
    private val parseHostInput: ParseHostInputUseCase,
    private val validate: ValidateTunnelFormUseCase
) : ViewModel() {

    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        devices: ConnectedDevices,
        repository: SavedTunnelRepository,
        controller: TunnelController,
        tester: TunnelTester,
        ports: PortAvailability,
        localNetwork: LocalNetworkGate,
        settings: SettingsRepository,
        parseHostInput: ParseHostInputUseCase,
        validate: ValidateTunnelFormUseCase
    ) : this(
        tunnelId = savedStateHandle.toRoute<NavRoutes.TunnelEditorScreen>().tunnelId,
        transport = savedStateHandle.toRoute<NavRoutes.TunnelEditorScreen>().transport,
        devices = devices,
        repository = repository,
        controller = controller,
        tester = tester,
        ports = ports,
        localNetwork = localNetwork,
        settings = settings,
        parseHostInput = parseHostInput,
        validate = validate
    )

    private val editingId = tunnelId ?: UUID.randomUUID().toString()
    private var otherTunnelPorts: Map<Int, String> = emptyMap()
    private var originalLocalPort: Int? = null
    private var hostCheck: Job? = null
    private var portCheck: Job? = null
    private var testJob: Job? = null

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<TunnelEditorUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TunnelEditorEvent>()
    val events: SharedFlow<TunnelEditorEvent> = _events.asSharedFlow()

    init {
        transport?.let(::observeDevice)
        observeOtherTunnels()
        observeRecentHosts()
        observeLanSharing()
        observeRunning()
        tunnelId?.let(::load)
    }

    fun onAddressChange(text: String) {
        val parsed = parseHostInput(text)
        updateForm { form -> form.copy(address = text).let { base -> parsed?.let { base.withParsed(it) } ?: base } }
        if (parsed != null) scheduleChecks()
    }

    fun onHostChange(text: String) {
        updateForm { it.copy(host = text) }
        scheduleHostCheck()
    }

    fun onTargetPortChange(text: String) {
        updateForm { it.copy(targetPort = text, localPort = if (it.sameAsTarget) text else it.localPort) }
        schedulePortCheck()
    }

    fun onLocalPortChange(text: String) {
        updateForm { it.copy(localPort = text, sameAsTarget = text == it.targetPort) }
        schedulePortCheck()
    }

    fun onSameAsTargetChange(enabled: Boolean) {
        updateForm { it.copy(sameAsTarget = enabled, localPort = if (enabled) it.targetPort else it.localPort) }
        schedulePortCheck()
    }

    fun onEndpointTypeChange(type: DeviceEndpointType) {
        updateForm { it.copy(endpointType = type, sameAsTarget = type == DeviceEndpointType.TCP && it.sameAsTarget) }
    }

    fun onEndpointValueChange(text: String) {
        updateForm {
            val follows = it.sameAsTarget && it.endpointType == DeviceEndpointType.TCP
            it.copy(endpointValue = text, localPort = if (follows) text else it.localPort)
        }
        schedulePortCheck()
    }

    /** Fills the form from [preset], naming the tunnel [name] unless the user already named it. */
    fun onPresetSelected(preset: DevicePreset, name: String) {
        updateForm {
            it.copy(
                endpointType = preset.endpoint.type,
                endpointValue = preset.endpoint.value,
                localPort = preset.localPort.toString(),
                sameAsTarget = preset.endpoint.value == preset.localPort.toString(),
                looksLikeHttp = preset.looksLikeHttp,
                name = it.name.ifBlank { name }
            )
        }
        schedulePortCheck()
    }

    fun onNameChange(text: String) = updateForm { it.copy(name = text) }

    fun onAutoStartChange(enabled: Boolean) = updateForm { it.copy(autoStart = enabled) }

    fun onShareOnNetworkChange(enabled: Boolean) = updateForm { it.copy(shareOnNetwork = enabled) }

    fun onRecentHostSelected(host: RecentHost) {
        updateForm {
            it.copy(
                address = "",
                host = host.host,
                targetPort = host.lastPort.toString(),
                localPort = if (it.sameAsTarget) host.lastPort.toString() else it.localPort
            )
        }
        scheduleChecks()
    }

    fun useSuggestedPort(port: Int) = onLocalPortChange(port.toString())

    fun test() {
        val spec = validSpecOrShowErrors() ?: return
        testJob?.cancel()
        testJob = viewModelScope.launch {
            _state.update { it.copy(test = TestState.Testing) }
            val result = tester.test(spec.target)
            _state.update { it.withTestResult(result) }
        }
    }

    /** Saves the tunnel, and starts or restarts it when [start] is true. */
    fun save(start: Boolean) {
        val spec = validSpecOrShowErrors() ?: return
        val portStatus = _state.value.localPortStatus
        if (start && portStatus is LocalPortStatus.InUse) {
            _state.update { it.copy(startError = TunnelError.PortInUse(portStatus.suggestedPort)) }
            return
        }
        viewModelScope.launch { persist(spec, start) }
    }

    private suspend fun persist(spec: TunnelSpec, start: Boolean) {
        _state.update { it.copy(isSaving = true) }
        if (repository.save(spec).isFailure) {
            _state.update { it.copy(isSaving = false) }
            _events.emit(TunnelEditorEvent.SaveFailed)
            return
        }
        rememberHost(spec)
        if (!start) {
            _events.emit(TunnelEditorEvent.Saved(spec, started = false))
            return
        }
        controller.start(spec)
            .onSuccess { _events.emit(TunnelEditorEvent.Saved(spec, started = true)) }
            .onFailure { error -> _state.update { it.copy(isSaving = false, startError = error.toTunnelError()) } }
    }

    private fun initialState(): TunnelEditorUiState {
        val device = transport?.let(::connectedDevice)
        val form = device?.let { TunnelEditorForm(kind = EditorKind.Device(it)) } ?: TunnelEditorForm()
        return TunnelEditorUiState(
            form = form,
            isEditing = tunnelId != null,
            validation = validate(form),
            deviceConnected = device != null
        )
    }

    private fun connectedDevice(transport: ExternalDeviceTransport): EditorDevice? {
        val state = devices.state(transport).value as? ExternalDeviceState.Connected ?: return null
        return EditorDevice(transport, state.deviceId, state.displayName)
    }

    private fun observeDevice(transport: ExternalDeviceTransport) {
        viewModelScope.launch {
            devices.state(transport).collect { state ->
                _state.update { it.copy(deviceConnected = state is ExternalDeviceState.Connected) }
            }
        }
    }

    private suspend fun rememberHost(spec: TunnelSpec) {
        val target = spec.target
        if (target is TunnelTarget.NetworkHost) repository.rememberHost(RecentHost(target.host, target.port))
    }

    private fun validSpecOrShowErrors(): TunnelSpec? {
        val current = _state.value
        if (!current.validation.isValid) {
            _state.update { it.copy(showErrors = true) }
            return null
        }
        return current.form.toSpecOrNull(editingId)
    }

    private fun updateForm(transform: (TunnelEditorForm) -> TunnelEditorForm) {
        _state.update { current ->
            val form = transform(current.form)
            val targetChanged = form.targetKey() != current.form.targetKey()
            if (targetChanged) testJob?.cancel()
            current.copy(
                form = form,
                validation = validate(form),
                test = if (targetChanged) TestState.Idle else current.test,
                startError = null
            )
        }
    }

    private fun validate(form: TunnelEditorForm): ValidateTunnelFormUseCase.Result = when (form.kind) {
        EditorKind.Network -> validate(
            ValidateTunnelFormUseCase.Input(
                host = form.host,
                targetPort = form.targetPort,
                localPort = form.localPort,
                portsInUse = otherTunnelPorts
            )
        )

        is EditorKind.Device -> validate.device(
            ValidateTunnelFormUseCase.DeviceInput(
                endpointType = form.endpointType,
                endpointValue = form.endpointValue,
                localPort = form.localPort,
                portsInUse = otherTunnelPorts
            )
        )
    }

    private fun scheduleChecks() {
        scheduleHostCheck()
        schedulePortCheck()
    }

    private fun scheduleHostCheck() {
        hostCheck?.cancel()
        hostCheck = viewModelScope.launch {
            delay(CHECK_DEBOUNCE_MS)
            if (_state.value.isDevice) return@launch
            val host = _state.value.form.host.trim()
            val isLocal = host.isNotEmpty() && localNetwork.isLocal(host)
            _state.update { it.copy(targetIsLocal = isLocal) }
        }
    }

    private fun schedulePortCheck() {
        portCheck?.cancel()
        portCheck = viewModelScope.launch {
            val port = _state.value.form.localPort.trim().toIntOrNull()
            if (port == null) {
                _state.update { it.copy(localPortStatus = LocalPortStatus.Unknown) }
                return@launch
            }
            _state.update { it.copy(localPortStatus = LocalPortStatus.Checking) }
            delay(CHECK_DEBOUNCE_MS)
            _state.update { it.copy(localPortStatus = portStatus(port)) }
        }
    }

    private suspend fun portStatus(port: Int): LocalPortStatus = when {
        _state.value.isRunning && port == originalLocalPort -> LocalPortStatus.Free
        ports.isFree(port) -> LocalPortStatus.Free
        else -> LocalPortStatus.InUse(ports.nextFree(port))
    }

    private fun load(id: String) {
        viewModelScope.launch {
            val spec = repository.tunnel(id).getOrNull() ?: return@launch
            originalLocalPort = spec.localPort
            updateForm { spec.toForm() }
            (spec.target as? TunnelTarget.Device)?.let { observeDevice(it.transport) }
            scheduleChecks()
        }
    }

    private fun observeOtherTunnels() {
        viewModelScope.launch {
            repository.tunnels().collect { tunnels ->
                otherTunnelPorts = tunnels.filter { it.id != editingId }.associate { it.localPort to it.name }
                _state.update { it.copy(validation = validate(it.form)) }
            }
        }
    }

    private fun observeRecentHosts() {
        viewModelScope.launch {
            repository.recentHosts().collect { hosts -> _state.update { it.copy(recentHosts = hosts) } }
        }
    }

    private fun observeLanSharing() {
        viewModelScope.launch {
            settings.getBoolean(SettingsKeys.PortForwardAllowLanSharing).collect { allowed ->
                _state.update { it.copy(lanSharingAllowed = allowed) }
            }
        }
    }

    private fun observeRunning() {
        viewModelScope.launch {
            controller.runtimes.collect { runtimes ->
                val running = runtimes[editingId]?.state?.isRunning == true
                _state.update { it.copy(isRunning = running) }
            }
        }
    }

    private fun TunnelEditorUiState.withTestResult(result: TestResult): TunnelEditorUiState {
        val speaksHttp = result is TestResult.Reachable && result.httpStatus != null
        val updatedForm = if (speaksHttp) form.copy(looksLikeHttp = true) else form
        return copy(test = TestState.Done(result), form = updatedForm)
    }

    private fun TunnelEditorForm.withParsed(parsed: ParsedHostInput): TunnelEditorForm {
        val parsedPort = parsed.port?.toString()
        return copy(
            host = parsed.host,
            targetPort = parsedPort ?: targetPort,
            path = parsed.path,
            looksLikeHttp = parsed.isHttp || looksLikeHttp,
            localPort = if (sameAsTarget && parsedPort != null) parsedPort else localPort
        )
    }

    private fun TunnelEditorForm.targetKey() = targetOrNull()

    private companion object {
        const val CHECK_DEBOUNCE_MS = 400L
    }
}
