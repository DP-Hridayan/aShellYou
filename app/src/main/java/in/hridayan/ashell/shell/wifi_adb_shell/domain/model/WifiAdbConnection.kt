package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton to manage WifiAdb connection states.
 * Tracks both global state and per-device states.
 */
object WifiAdbConnection {
    // Global state for backwards compatibility and overall status
    private val _state = MutableStateFlow<WifiAdbState>(WifiAdbState.None)
    val state = _state.asStateFlow()

    // Per-device connection states
    private val _deviceStates = MutableStateFlow<Map<String, WifiAdbState>>(emptyMap())
    val deviceStates = _deviceStates.asStateFlow()

    /**
     * Update the connection state.
     * If the state has a deviceId, also updates the per-device state map.
     */
    fun updateState(newState: WifiAdbState) {
        _state.value = newState
        // Also update per-device map if state has deviceId
        newState.deviceId?.let { deviceId ->
            _deviceStates.value = _deviceStates.value + (deviceId to newState)
        }
    }

    /**
     * Get the current state for a specific device.
     */
    fun getDeviceState(deviceId: String): WifiAdbState? {
        return _deviceStates.value[deviceId]
    }

    /**
     * Clear the state for a specific device.
     */
    fun clearDeviceState(deviceId: String) {
        _deviceStates.value = _deviceStates.value - deviceId
    }
    
    /**
     * Set a device to disconnected state.
     */
    fun setDeviceDisconnected(deviceId: String) {
        val newState = WifiAdbState.Disconnected(device = deviceId)
        _deviceStates.value = _deviceStates.value + (deviceId to newState)
        // Also update global state if this was the currently connected device
        val currentState = _state.value
        if (currentState.deviceId == deviceId) {
            _state.value = newState
        }
    }

    val currentState: WifiAdbState get() = _state.value
}