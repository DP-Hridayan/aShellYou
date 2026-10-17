package `in`.hridayan.ashell.portforward.data.startup

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.portforward.data.device.DeviceChannels
import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.model.TunnelSpec
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.reaches
import `in`.hridayan.ashell.portforward.domain.repository.PortForwardSettingsSource
import `in`.hridayan.ashell.portforward.domain.repository.SavedTunnelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Starts tunnels marked to start automatically: network tunnels when the app opens, and device
 * tunnels whenever their device connects.
 *
 * Tunnels the system stopped are left alone: the Ports screen asks before restarting them, so an
 * app that keeps being killed does not restart them in a loop.
 */
@Singleton
class TunnelAutoStarter @Inject constructor(
    private val repository: SavedTunnelRepository,
    private val controller: TunnelController,
    private val settings: PortForwardSettingsSource,
    private val devices: DeviceChannels
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val started = AtomicBoolean(false)

    /** Call when the user opens the app, from a foreground activity. */
    fun onAppOpened() {
        if (!started.compareAndSet(false, true)) return
        scope.launch { startMatching { it.target is TunnelTarget.NetworkHost || it.reachesConnectedDevice() } }
        ExternalDeviceTransport.entries.forEach { transport -> scope.launch { watch(transport) } }
    }

    private suspend fun watch(transport: ExternalDeviceTransport) {
        devices.state(transport)
            .filterIsInstance<ExternalDeviceState.Connected>()
            .distinctUntilChanged()
            .collect { connected -> startMatching { it.startsWith(connected, transport) } }
    }

    private suspend fun startMatching(predicate: (TunnelSpec) -> Boolean) {
        if (!settings.current().autoStart) return
        val interrupted = repository.wasRunningIds().first()
        val running = controller.runtimes.value.keys
        repository.tunnels().first()
            .filter { it.autoStart && it.id !in interrupted && it.id !in running && predicate(it) }
            .forEach { controller.start(it) }
    }

    private fun TunnelSpec.startsWith(connected: ExternalDeviceState, transport: ExternalDeviceTransport): Boolean {
        val device = target as? TunnelTarget.Device ?: return false
        return device.transport == transport && connected.reaches(device)
    }

    private fun TunnelSpec.reachesConnectedDevice(): Boolean {
        val device = target as? TunnelTarget.Device ?: return false
        return devices.isReachable(device)
    }
}
