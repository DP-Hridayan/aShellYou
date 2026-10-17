package `in`.hridayan.ashell.portforward.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.model.isRunning
import `in`.hridayan.ashell.portforward.domain.repository.SavedTunnelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A confirmation the settings page is asking for. */
sealed interface PortForwardSettingsDialog {
    data object EnableLanSharing : PortForwardSettingsDialog
    data class DisableLanSharing(val sharedRunningCount: Int) : PortForwardSettingsDialog
    data object ConnectTimeout : PortForwardSettingsDialog
    data object MaxConnections : PortForwardSettingsDialog
    data object WaitTimeout : PortForwardSettingsDialog
    data object ClearRecentHosts : PortForwardSettingsDialog
}

/**
 * Backs the port-forwarding settings page.
 *
 * Turning sharing off restarts the tunnels that were shared, so they stop listening on the network
 * at once instead of on their next start.
 */
@HiltViewModel
class PortForwardSettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val controller: TunnelController,
    private val repository: SavedTunnelRepository
) : ViewModel() {

    private val _dialog = MutableStateFlow<PortForwardSettingsDialog?>(null)
    val dialog: StateFlow<PortForwardSettingsDialog?> = _dialog.asStateFlow()

    fun onLanSharingClick(currentlyAllowed: Boolean) {
        _dialog.value = when {
            !currentlyAllowed -> PortForwardSettingsDialog.EnableLanSharing
            sharedRunning().isEmpty() -> null.also { setLanSharing(false) }
            else -> PortForwardSettingsDialog.DisableLanSharing(sharedRunning().size)
        }
    }

    fun confirmEnableLanSharing() {
        dismissDialog()
        setLanSharing(true)
    }

    fun confirmDisableLanSharing() {
        dismissDialog()
        viewModelScope.launch {
            val shared = sharedRunning()
            settings.setBoolean(SettingsKeys.PortForwardAllowLanSharing, false)
            shared.forEach { controller.start(it) }
        }
    }

    fun showDialog(dialog: PortForwardSettingsDialog) {
        _dialog.value = dialog
    }

    fun dismissDialog() {
        _dialog.value = null
    }

    fun setConnectTimeout(seconds: Int) = setInt(SettingsKeys.PortForwardConnectTimeoutSeconds, seconds)

    fun setMaxConnections(count: Int) = setInt(SettingsKeys.PortForwardMaxConnections, count)

    fun setWaitTimeout(minutes: Int) = setInt(SettingsKeys.PortForwardWaitTimeoutMinutes, minutes)

    fun clearRecentHosts() {
        dismissDialog()
        viewModelScope.launch { repository.clearRecentHosts() }
    }

    private fun setInt(key: SettingsKeys<Int>, value: Int) {
        dismissDialog()
        viewModelScope.launch { settings.setInt(key, value) }
    }

    private fun setLanSharing(allowed: Boolean) {
        viewModelScope.launch { settings.setBoolean(SettingsKeys.PortForwardAllowLanSharing, allowed) }
    }

    private fun sharedRunning() = controller.runtimes.value.values
        .filter { it.state.isRunning && it.spec.shareOnNetwork }
        .map { it.spec }
}
