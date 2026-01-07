package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbConnection
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.DiscoveredPairingService
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WifiAdbViewModel @Inject constructor(
    private val wifiAdbRepository: WifiAdbRepository
) : ViewModel() {
    private val _ipAddress = MutableStateFlow("")
    val ipAddress: StateFlow<String> = _ipAddress

    private val _pairingPort = MutableStateFlow("")
    val pairingPort: StateFlow<String> = _pairingPort

    private val _pairingCode = MutableStateFlow("")
    val pairingCode: StateFlow<String> = _pairingCode

    private val _connectPort = MutableStateFlow("")
    val connectPort: StateFlow<String> = _connectPort

    private val _ipAddressError = MutableStateFlow(false)
    val ipAddressError: StateFlow<Boolean> = _ipAddressError

    private val _pairingPortError = MutableStateFlow(false)
    val pairingPortError: StateFlow<Boolean> = _pairingPortError

    private val _pairingCodeError = MutableStateFlow(false)
    val pairingCodeError: StateFlow<Boolean> = _pairingCodeError

    private val _connectPortError = MutableStateFlow(false)
    val connectPortError: StateFlow<Boolean> = _connectPortError

    val state: StateFlow<WifiAdbState> = WifiAdbConnection.state

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap

    // Discovered pairing services for "Pair Using Code" tab
    private val _discoveredPairingServices = MutableStateFlow<List<DiscoveredPairingService>>(emptyList())
    val discoveredPairingServices: StateFlow<List<DiscoveredPairingService>> = _discoveredPairingServices.asStateFlow()

    /**
     * Get the current state for a specific device.
     */
    fun getDeviceState(deviceId: String): WifiAdbState? = WifiAdbConnection.getDeviceState(deviceId)

    /**
     * Set a device to disconnected state.
     * Used when WiFi is lost or device should be marked as disconnected.
     */
    fun setDeviceDisconnected(deviceId: String) = WifiAdbConnection.setDeviceDisconnected(deviceId)

    private val _savedDevices = MutableStateFlow<List<WifiAdbDevice>>(emptyList())
    val savedDevices = _savedDevices.asStateFlow()

    // Track currently connected device - observe from WifiAdbConnection for cross-component updates
    val currentDevice: StateFlow<WifiAdbDevice?> = WifiAdbConnection.currentDevice

    init {
        viewModelScope.launch {
            wifiAdbRepository.getSavedDevicesFlow().collect { devices ->
                _savedDevices.value = devices
            }
        }

        // Also sync WifiAdbConnection when current device changes
        viewModelScope.launch {
            WifiAdbConnection.currentDevice.collect { device ->
                if (device != null) {
                    wifiAdbRepository.getCurrentDevice()?.let { repoDevice ->
                        if (WifiAdbConnection.currentDevice.value?.id != repoDevice.id) {
                            WifiAdbConnection.setCurrentDevice(repoDevice)
                        }
                    }
                }
            }
        }

        // Auto-start/stop heartbeat based on connection state
        viewModelScope.launch {
            WifiAdbConnection.state.collect { state ->
                when (state) {
                    is WifiAdbState.ConnectSuccess -> {
                        wifiAdbRepository.startHeartbeat()
                    }

                    is WifiAdbState.Disconnected,
                    is WifiAdbState.ConnectFailed,
                    is WifiAdbState.None -> {
                        wifiAdbRepository.stopHeartbeat()
                    }

                    else -> { /* Keep heartbeat running for other states */
                    }
                }
            }
        }
    }


    fun onIpChange(newValue: String) {
        _ipAddress.value = newValue
        _ipAddressError.value = false
    }

    fun onPairingPortChange(newValue: String) {
        _pairingPort.value = newValue
        _pairingPortError.value = false
    }

    fun onPairingCodeChange(newValue: String) {
        _pairingCode.value = newValue
        _pairingCodeError.value = false
    }

    fun onConnectingPortChange(newValue: String) {
        _connectPort.value = newValue
        _connectPortError.value = false
    }


    fun startPairingManually() {
        checkInputFieldValidity()

        if (pairingFieldsInvalid) {
            WifiAdbConnection.updateState(WifiAdbState.PairingFailed("Invalid fields"))
            return
        }
        val ip = _ipAddress.value
        val port = _pairingPort.value.toIntOrNull() ?: return
        val code = _pairingCode.value.trim()

        if (code.isEmpty() || !code.all { it.isDigit() }) return

        startPairing(ip, port, code)
    }

    private fun startPairing(ip: String, port: Int, code: String) {

        WifiAdbConnection.updateState(WifiAdbState.PairingStarted())

        wifiAdbRepository.pair(ip, port, code, object : WifiAdbRepositoryImpl.PairingListener {
            override fun onPairingSuccess() {
                WifiAdbConnection.updateState(WifiAdbState.PairingSuccess("Paired successfully"))
            }

            override fun onPairingFailed() {
                WifiAdbConnection.updateState(WifiAdbState.PairingFailed("Pairing failed"))
            }
        })
    }

    fun startMdnsPairing(pairingCode: String) {
        wifiAdbRepository.discoverAdbPairingService(
            pairingCode,
            autoPair = true,
            callback = object :
                WifiAdbRepositoryImpl.MdnsDiscoveryCallback {
                override fun onServiceFound(name: String, ip: String, port: Int) {
                    WifiAdbConnection.updateState(WifiAdbState.PairingStarted())
                }

                override fun onPairingSuccess(ip: String, port: Int) {
                    WifiAdbConnection.updateState(WifiAdbState.PairingSuccess(ip))
                }

                override fun onPairingFailed(ip: String, port: Int) {
                    WifiAdbConnection.updateState(WifiAdbState.PairingFailed(ip))
                }

                override fun onServiceLost(name: String) {
                    Log.d("ADB", "Service lost: $name")
                }

                override fun onError(e: Throwable) {
                    Log.e("ADB", "Error: ${e.message}")
                }
            })
    }

    fun stopMdnsDiscovery() {
        wifiAdbRepository.stopMdnsDiscovery()
    }

    fun startConnectingManually() {
        checkInputFieldValidity()

        if (connectFieldsInvalid) {
            WifiAdbConnection.updateState(WifiAdbState.PairConnectFailed("Invalid fields"))
            return
        }
        val ip = _ipAddress.value
        val port = _connectPort.value.toIntOrNull() ?: return

        startConnecting(ip, port)
    }


    private fun startConnecting(ip: String, port: Int) {
        WifiAdbConnection.updateState(WifiAdbState.ConnectStarted())

        wifiAdbRepository.connect(ip, port, object : WifiAdbRepositoryImpl.ConnectionListener {
            override fun onConnectionSuccess() {
                WifiAdbConnection.updateState(WifiAdbState.ConnectSuccess("Connected!"))
            }

            override fun onConnectionFailed() {
                WifiAdbConnection.updateState(WifiAdbState.PairConnectFailed("Connection failed"))
            }
        })
    }

    fun reconnectToDevice(device: WifiAdbDevice) {
        wifiAdbRepository.reconnect(device, object : WifiAdbRepositoryImpl.ReconnectListener {
            override fun onReconnectSuccess() {
                WifiAdbConnection.setCurrentDevice(device)
                WifiAdbConnection.updateState(WifiAdbState.ConnectSuccess(device.id, device.id))
            }

            override fun onReconnectFailed(requiresPairing: Boolean) {
                WifiAdbConnection.setCurrentDevice(null)
                if (requiresPairing) {
                    // Prefill IP for re-pairing
                    _ipAddress.value = device.ip
                }
            }
        })
    }

    fun cancelReconnect() {
        wifiAdbRepository.cancelReconnect()
    }

    fun disconnect() {
        wifiAdbRepository.disconnect()
        WifiAdbConnection.setCurrentDevice(null)
    }

    fun forgetDevice(device: WifiAdbDevice) {
        wifiAdbRepository.forgetDevice(device)
    }

    fun isConnected(): Boolean = wifiAdbRepository.isConnected()

    fun generateQr(sessionId: String, pairingCode: String) {
        viewModelScope.launch {
            try {
                val bitmap = wifiAdbRepository.generatePairingQR(
                    sessionId = sessionId,
                    pairingCode = pairingCode
                )
                _qrBitmap.value = bitmap
            } catch (e: Exception) {
                Log.e("Failed to generate QR", e.message.toString())
            }
        }
    }

    private fun checkInputFieldValidity() {
        _ipAddressError.value = _ipAddress.value.isEmpty()
        _pairingPortError.value = _pairingPort.value.isEmpty()
        _pairingCodeError.value = _pairingCode.value.isEmpty()
        _connectPortError.value = _connectPort.value.isEmpty()
    }

    private val pairingFieldsInvalid: Boolean
        get() = _ipAddressError.value || _pairingPortError.value || _pairingCodeError.value

    private val connectFieldsInvalid: Boolean
        get() = _ipAddressError.value || _connectPortError.value

    // ============ "Pair Using Code" Tab Methods ============

    /**
     * Start discovery for both pairing and connect services.
     * Used when "Pair Using Code" tab is opened.
     */
    fun startCodePairingDiscovery() {
        _discoveredPairingServices.value = emptyList()
        wifiAdbRepository.startCodePairingDiscovery(
            onPairingServiceFound = { service ->
                val current = _discoveredPairingServices.value.toMutableList()
                // Avoid duplicates
                if (current.none { it.key == service.key }) {
                    current.add(service)
                    _discoveredPairingServices.value = current
                }
            },
            onPairingServiceLost = { serviceName ->
                val current = _discoveredPairingServices.value.toMutableList()
                current.removeAll { it.serviceName == serviceName }
                _discoveredPairingServices.value = current
            }
        )
    }

    /**
     * Stop code pairing discovery.
     */
    fun stopCodePairingDiscovery() {
        wifiAdbRepository.stopCodePairingDiscovery()
        _discoveredPairingServices.value = emptyList()
    }

    /**
     * Pair with a discovered device using the entered pairing code.
     * Uses cached connect port for immediate connection after pairing.
     */
    fun pairWithCode(service: DiscoveredPairingService, pairingCode: String) {
        if (pairingCode.length != 6) return

        WifiAdbConnection.updateState(WifiAdbState.PairingStarted())
        
        wifiAdbRepository.pairAndConnect(
            ip = service.ip,
            pairingPort = service.port,
            pairingCode = pairingCode,
            callback = object : WifiAdbRepositoryImpl.MdnsDiscoveryCallback {
                override fun onPairingSuccess(ip: String, port: Int) {
                    WifiAdbConnection.updateState(WifiAdbState.ConnectSuccess("$ip:$port"))
                }

                override fun onPairingFailed(ip: String, port: Int) {
                    WifiAdbConnection.updateState(WifiAdbState.PairConnectFailed("Pairing failed"))
                }

                override fun onServiceFound(name: String, ip: String, port: Int) {}
                override fun onServiceLost(name: String) {}
                override fun onError(e: Throwable) {}
            }
        )
    }

    /**
     * Add a discovered pairing service (called from repository callback).
     */
    fun addDiscoveredPairingService(service: DiscoveredPairingService) {
        val current = _discoveredPairingServices.value.toMutableList()
        if (current.none { it.key == service.key }) {
            current.add(service)
            _discoveredPairingServices.value = current
        }
    }

    /**
     * Remove a discovered pairing service when it goes away.
     */
    fun removeDiscoveredPairingService(serviceName: String) {
        val current = _discoveredPairingServices.value.toMutableList()
        current.removeAll { it.serviceName == serviceName }
        _discoveredPairingServices.value = current
    }
}
