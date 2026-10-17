package `in`.hridayan.ashell.portforward.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpoint
import `in`.hridayan.ashell.portforward.domain.model.DeviceEndpointType
import `in`.hridayan.ashell.portforward.domain.model.DevicePreset
import `in`.hridayan.ashell.portforward.domain.model.TunnelRuntime
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.repository.ConnectedDevices
import `in`.hridayan.ashell.portforward.domain.repository.PortAvailability
import `in`.hridayan.ashell.portforward.domain.repository.SavedTunnelRepository
import `in`.hridayan.ashell.portforward.domain.usecase.ObserveInterruptedTunnelsUseCase
import `in`.hridayan.ashell.portforward.presentation.model.PortsEvent
import `in`.hridayan.ashell.portforward.presentation.model.PortsScope
import `in`.hridayan.ashell.portforward.presentation.model.PortsUiState
import `in`.hridayan.ashell.portforward.presentation.model.TunnelItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** Backs the list of tunnels: starting, stopping, deleting and recovering them. */
@HiltViewModel
class PortsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SavedTunnelRepository,
    private val controller: TunnelController,
    private val devices: ConnectedDevices,
    private val ports: PortAvailability,
    observeInterrupted: ObserveInterruptedTunnelsUseCase
) : ViewModel() {

    private val scope = MutableStateFlow(
        savedStateHandle.toRoute<NavRoutes.PortsScreen>().transport?.let { PortsScope.Device(it) } ?: PortsScope.All
    )

    val state: StateFlow<PortsUiState> = combine(
        repository.tunnels(),
        controller.runtimes,
        observeInterrupted(),
        deviceStates(),
        scope,
        ::buildState
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PortsUiState(scope = scope.value))

    private val _events = MutableSharedFlow<PortsEvent>()
    val events: SharedFlow<PortsEvent> = _events.asSharedFlow()

    fun setScope(newScope: PortsScope) {
        scope.value = newScope
    }

    fun setRunning(spec: TunnelSpec, running: Boolean) {
        viewModelScope.launch {
            if (!running) {
                controller.stop(spec.id)
                return@launch
            }
            if (controller.start(spec).isSuccess) _events.emit(PortsEvent.Started(spec))
        }
    }

    fun stopAll() {
        viewModelScope.launch {
            val stopped = controller.stopAll()
            if (stopped.isNotEmpty()) _events.emit(PortsEvent.StoppedAll(stopped))
        }
    }

    fun startAll(specs: List<TunnelSpec>) {
        viewModelScope.launch { specs.forEach { controller.start(it) } }
    }

    fun delete(spec: TunnelSpec) {
        viewModelScope.launch {
            val wasRunning = state.value.item(spec.id)?.isRunning == true
            controller.stop(spec.id)
            controller.dismiss(spec.id)
            if (repository.delete(spec.id).isSuccess) _events.emit(PortsEvent.Deleted(spec, wasRunning))
        }
    }

    fun restore(spec: TunnelSpec, start: Boolean) {
        viewModelScope.launch {
            if (repository.save(spec).isSuccess && start) controller.start(spec)
        }
    }

    /** Moves a tunnel whose port is taken to [port], and starts it there. */
    fun moveToPort(spec: TunnelSpec, port: Int) = saveAndStart(spec.copy(localPort = port))

    /** Points a device tunnel at the device now connected on its transport, and starts it. */
    fun useConnectedDevice(spec: TunnelSpec, device: ExternalDeviceState.Connected) {
        val target = spec.target as? TunnelTarget.Device ?: return
        saveAndStart(spec.copy(target = target.copy(deviceId = device.deviceId, deviceName = device.displayName)))
    }

    /** Forwards [devicePort] on the scoped device to the same local port, or the next free one. */
    fun quickForward(devicePort: Int, name: String) {
        val endpoint = DeviceEndpoint(DeviceEndpointType.TCP, devicePort.toString())
        quickStart(endpoint, devicePort, looksLikeHttp = false, name = name)
    }

    fun quickPreset(preset: DevicePreset, name: String) =
        quickStart(preset.endpoint, preset.localPort, preset.looksLikeHttp, name)

    fun restartInterrupted() = startAll(state.value.interrupted)

    fun dismissInterrupted() {
        viewModelScope.launch {
            repository.setWasRunning(state.value.interrupted.map { it.id }, wasRunning = false)
        }
    }

    private fun quickStart(endpoint: DeviceEndpoint, preferredPort: Int, looksLikeHttp: Boolean, name: String) {
        val transport = (state.value.scope as? PortsScope.Device)?.transport ?: return
        val device = state.value.connectedDevices[transport] ?: return
        viewModelScope.launch {
            val localPort = freePortNear(preferredPort) ?: return@launch
            val spec = TunnelSpec(
                id = UUID.randomUUID().toString(),
                name = name,
                localPort = localPort,
                shareOnNetwork = false,
                target = TunnelTarget.Device(transport, device.deviceId, device.displayName, endpoint),
                autoStart = false,
                looksLikeHttp = looksLikeHttp
            )
            if (repository.save(spec).isSuccess && controller.start(spec).isSuccess) {
                _events.emit(PortsEvent.Started(spec))
            }
        }
    }

    private suspend fun freePortNear(port: Int): Int? = if (ports.isFree(port)) port else ports.nextFree(port)

    private fun saveAndStart(spec: TunnelSpec) {
        viewModelScope.launch {
            if (repository.save(spec).isSuccess) controller.start(spec)
        }
    }

    private fun deviceStates(): Flow<Map<ExternalDeviceTransport, ExternalDeviceState>> = combine(
        devices.state(ExternalDeviceTransport.OTG),
        devices.state(ExternalDeviceTransport.WIFI_ADB)
    ) { otg, wifi -> mapOf(ExternalDeviceTransport.OTG to otg, ExternalDeviceTransport.WIFI_ADB to wifi) }

    private fun buildState(
        tunnels: List<TunnelSpec>,
        runtimes: Map<String, TunnelRuntime>,
        interrupted: List<TunnelSpec>,
        deviceStates: Map<ExternalDeviceTransport, ExternalDeviceState>,
        currentScope: PortsScope
    ): PortsUiState {
        val items = tunnels
            .filter { it.isIn(currentScope) }
            .map { TunnelItem(it, runtimes[it.id], it.transport()?.let(deviceStates::get)) }
        val (running, saved) = items.partition { it.isRunning }
        return PortsUiState(
            isLoaded = true,
            scope = currentScope,
            running = running,
            saved = saved,
            interrupted = interrupted,
            devices = deviceStates
        )
    }

    private fun TunnelSpec.transport(): ExternalDeviceTransport? = (target as? TunnelTarget.Device)?.transport

    private fun TunnelSpec.isIn(scope: PortsScope): Boolean = when (scope) {
        PortsScope.All -> true
        PortsScope.Network -> target is TunnelTarget.NetworkHost
        is PortsScope.Device -> transport() == scope.transport
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
