package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton managing WiFi ADB connection state and events.
 *
 * This object serves as the central point for tracking ADB connection status,
 * broadcasting one-shot events, and managing per-device connection states.
 */
object WifiAdbConnection {
    private val _state = MutableStateFlow<WifiAdbState>(WifiAdbState.Idle)

    /**
     * The current persistent connection state.
     */
    val state = _state.asStateFlow()

    /**
     * The current connection state value.
     */
    val currentState: WifiAdbState
        get() = _state.value

    private val _events = MutableSharedFlow<WifiAdbEvent>(
        replay = 0,
        extraBufferCapacity = 10
    )

    /**
     * One-shot events for connection outcomes, errors, and navigation.
     */
    val events = _events.asSharedFlow()

    private val _currentDevice = MutableStateFlow<WifiAdbDevice?>(null)

    /**
     * The currently connected device, if any.
     */
    val currentDevice = _currentDevice.asStateFlow()

    private val _deviceStates = MutableStateFlow<Map<String, WifiAdbState>>(emptyMap())

    /**
     * A map of connection states for individual devices, keyed by device ID.
     */
    val deviceStates = _deviceStates.asStateFlow()

    /**
     * Updates the global connection state and the per-device state map.
     */
    fun updateState(newState: WifiAdbState) {
        _state.value = newState
        newState.currentDeviceId?.let { deviceId ->
            _deviceStates.value += (deviceId to newState)
        }
    }

    /**
     * Emits a one-shot event to be consumed by collectors.
     */
    suspend fun emitEvent(event: WifiAdbEvent) {
        _events.emit(event)
    }

    /**
     * Attempts to emit a one-shot event without suspending.
     *
     * @return True if the event was successfully emitted, false otherwise.
     */
    fun tryEmitEvent(event: WifiAdbEvent): Boolean {
        return _events.tryEmit(event)
    }

    /**
     * Sets the currently connected device.
     */
    fun setCurrentDevice(device: WifiAdbDevice?) {
        _currentDevice.value = device
    }

    /**
     * Returns the connection state for a specific device ID.
     */
    fun getDeviceState(deviceId: String): WifiAdbState? {
        return _deviceStates.value[deviceId]
    }

    /**
     * Marks a specific device as connected and updates the global state.
     */
    fun setDeviceConnected(deviceId: String, address: String) {
        val newState = WifiAdbState.Connected(deviceId = deviceId, address = address)
        _deviceStates.value += (deviceId to newState)
        _state.value = newState
    }

    /**
     * Marks a specific device as disconnected.
     *
     * If the disconnected device was the globally active one, the global state is updated.
     */
    fun setDeviceDisconnected(deviceId: String) {
        val newState = WifiAdbState.Disconnected(deviceId = deviceId)
        _deviceStates.value += (deviceId to newState)
        if (_state.value.currentDeviceId == deviceId) {
            _state.value = newState
        }
    }

    /**
     * Removes the stored connection state for a specific device.
     */
    fun clearDeviceState(deviceId: String) {
        _deviceStates.value -= deviceId
    }

    /**
     * Resets all connection states and cleared the current device.
     */
    fun reset() {
        _state.value = WifiAdbState.Idle
        _currentDevice.value = null
        _deviceStates.value = emptyMap()
    }
}
