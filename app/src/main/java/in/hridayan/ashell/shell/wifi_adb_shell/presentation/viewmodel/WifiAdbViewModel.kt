package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.DiscoveredPairingService
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbConnection
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
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
    val state: StateFlow<WifiAdbState> = WifiAdbConnection.state

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap

    private val _discoveredPairingServices =
        MutableStateFlow<List<DiscoveredPairingService>>(emptyList())
    val discoveredPairingServices: StateFlow<List<DiscoveredPairingService>> =
        _discoveredPairingServices.asStateFlow()

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


    fun reconnectToDevice(device: WifiAdbDevice) {
        wifiAdbRepository.reconnect(device, object : WifiAdbRepositoryImpl.ReconnectListener {
            override fun onReconnectSuccess() {
                WifiAdbConnection.setCurrentDevice(device)
                WifiAdbConnection.updateState(WifiAdbState.ConnectSuccess(device.id, device.id))
            }

            override fun onReconnectFailed(requiresPairing: Boolean) {
                WifiAdbConnection.setCurrentDevice(null)
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

    fun startQrPairDiscovery(pairingCode: String) {
        if (pairingCode.length != 6) return

        wifiAdbRepository.pairingWithQr(
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

    fun stopQrPairDiscovery() {
        wifiAdbRepository.stopMdnsDiscovery()
    }


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

        disconnect()

        wifiAdbRepository.pairAndConnect(
            ip = service.ip,
            pairingPort = service.port,
            pairingCode = pairingCode,
            callback = object : WifiAdbRepositoryImpl.MdnsDiscoveryCallback {
                override fun onPairingSuccess(ip: String, port: Int) {
                    WifiAdbConnection.updateState(WifiAdbState.ConnectSuccess("$ip:$port"))
                }

                override fun onPairingFailed(ip: String, port: Int) {
                }

                override fun onServiceFound(name: String, ip: String, port: Int) {}
                override fun onServiceLost(name: String) {
                }

                override fun onError(e: Throwable) {
                }
            }
        )
    }
}
