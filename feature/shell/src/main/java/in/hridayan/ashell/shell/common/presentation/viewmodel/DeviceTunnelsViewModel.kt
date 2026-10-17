package `in`.hridayan.ashell.shell.common.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.provider.DeviceTunnelControl
import `in`.hridayan.ashell.core.common.domain.provider.TunnelStatusProvider
import `in`.hridayan.ashell.core.common.domain.provider.TunnelStatusSummary
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The port-forwarding tunnels that reach the shell's device: how many there are, for the dock badge,
 * and stopping them before the user disconnects the device.
 */
@HiltViewModel
class DeviceTunnelsViewModel @Inject constructor(
    tunnelStatus: TunnelStatusProvider,
    private val tunnelControl: DeviceTunnelControl
) : ViewModel() {

    val summary: StateFlow<TunnelStatusSummary> = tunnelStatus.summary

    fun runningOn(transport: ExternalDeviceTransport): Int = summary.value.runningOn(transport)

    /** Stops the tunnels on [transport], then runs [then]. */
    fun stopTunnels(transport: ExternalDeviceTransport, then: () -> Unit) {
        viewModelScope.launch {
            tunnelControl.stopTunnels(transport)
            then()
        }
    }
}
