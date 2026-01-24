package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton managing WiFi ADB connection state and events.
 * 
 * ## Architecture:
 * - **States** ([state]): Persistent conditions observed via `StateFlow`
 * - **Events** ([events]): One-shot occurrences consumed via `SharedFlow`
 * 
 * ## Usage:
 * ```kotlin
 * // Observe persistent state for UI
 * val isConnected by WifiAdbConnection.state.collectAsState()
 * 
 * // Collect one-shot events
 * LaunchedEffect(Unit) {
 *     WifiAdbConnection.events.collect { event ->
 *         when (event) {
 *             is WifiAdbEvent.ConnectSuccess -> showToast("Connected!")
 *             is WifiAdbEvent.ReconnectFailed -> showDialog = true
 *         }
 *     }
 * }
 * ```
 */
object WifiAdbConnection {
    private val _state = MutableStateFlow<WifiAdbState>(WifiAdbState.Idle)
    
    /**
     * The current persistent connection state.
     * Use for UI elements that need to reflect current status (connection indicator, loading states).
     */
    val state = _state.asStateFlow()

    /**
     * Shortcut to get current state value synchronously.
     */
    val currentState: WifiAdbState
        get() = _state.value

    private val _events = MutableSharedFlow<WifiAdbEvent>(
        replay = 0,           // Don't replay to new collectors
        extraBufferCapacity = 10  // Buffer to prevent suspension during rapid events
    )
    
    /**
     * One-shot events for connection outcomes, errors, etc.
     * Collect in a LaunchedEffect to show dialogs, toasts, and handle navigation.
     * Events are consumed once and not replayed.
     */
    val events = _events.asSharedFlow()

    private val _currentDevice = MutableStateFlow<WifiAdbDevice?>(null)
    
    /**
     * The currently connected device, if any.
     * Shared across components for cross-screen state access.
     */
    val currentDevice = _currentDevice.asStateFlow()

    private val _deviceStates = MutableStateFlow<Map<String, WifiAdbState>>(emptyMap())
    
    /**
     * Per-device connection states for monitoring multiple saved devices.
     */
    val deviceStates = _deviceStates.asStateFlow()

    /**
     * Update the global connection state.
     * Also updates per-device state if the state has a deviceId.
     */
    fun updateState(newState: WifiAdbState) {
        _state.value = newState
        // Also update per-device map if state has deviceId
        newState.currentDeviceId?.let { deviceId ->
            _deviceStates.value += (deviceId to newState)
        }
    }

    /**
     * Emit a one-shot event.
     * Use for outcomes that should trigger UI actions once (dialogs, toasts).
     */
    suspend fun emitEvent(event: WifiAdbEvent) {
        _events.emit(event)
    }

    /**
     * Non-suspending version of emitEvent using tryEmit.
     * Returns true if event was emitted, false if buffer is full.
     */
    fun tryEmitEvent(event: WifiAdbEvent): Boolean {
        return _events.tryEmit(event)
    }


    /**
     * Set the currently connected device.
     */
    fun setCurrentDevice(device: WifiAdbDevice?) {
        _currentDevice.value = device
    }

    /**
     * Get the state for a specific device.
     */
    fun getDeviceState(deviceId: String): WifiAdbState? {
        return _deviceStates.value[deviceId]
    }

    /**
     * Mark a device as connected.
     */
    fun setDeviceConnected(deviceId: String, address: String) {
        val newState = WifiAdbState.Connected(deviceId = deviceId, address = address)
        _deviceStates.value += (deviceId to newState)
        _state.value = newState
    }

    /**
     * Mark a device as disconnected.
     */
    fun setDeviceDisconnected(deviceId: String) {
        val newState = WifiAdbState.Disconnected(deviceId = deviceId)
        _deviceStates.value += (deviceId to newState)
        // Update global state if this was the connected device
        if (_state.value.currentDeviceId == deviceId) {
            _state.value = newState
        }
    }

    /**
     * Clear the state for a specific device.
     */
    fun clearDeviceState(deviceId: String) {
        _deviceStates.value -= deviceId
    }

    /**
     * Reset all state to initial values.
     */
    fun reset() {
        _state.value = WifiAdbState.Idle
        _currentDevice.value = null
        _deviceStates.value = emptyMap()
    }
}
