package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbConnection
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.DiscoveredPairingService
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.PairingDiscoveryMode
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.SecureRandom
import javax.inject.Inject

private const val QR_SESSION_ID = "ashell_you"
private const val PAIRING_CODE_LENGTH = 6
private const val PAIRING_CODE_MIN = 100000
private const val PAIRING_CODE_RANGE = 900000
private const val QR_PAIRING_TIMEOUT_MS = 120_000L
private const val QR_GENERATION_ERROR_TAG = "WifiAdbViewModel"

@HiltViewModel
class WifiAdbViewModel @Inject constructor(
    private val wifiAdbRepository: WifiAdbRepository
) : ViewModel() {
    val state: StateFlow<WifiAdbState> = WifiAdbConnection.state

    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap

    private val _pairingCode = MutableStateFlow(generatePairingCode())
    val pairingCode: StateFlow<String> = _pairingCode.asStateFlow()

    private val _isQrExpired = MutableStateFlow(false)
    val isQrExpired: StateFlow<Boolean> = _isQrExpired.asStateFlow()

    private var discoveryMode = PairingDiscoveryMode.None
    private var qrExpiryJob: Job? = null
    private var generatedQrForCode: String? = null
    private var qrGenerationJob: Job? = null

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
    private val _lastConnectedDevice = MutableStateFlow<WifiAdbDevice?>(null)
    val lastConnectedDevice = _lastConnectedDevice.asStateFlow()

    init {
        _lastConnectedDevice.value = currentDevice.value

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

        viewModelScope.launch {
            WifiAdbConnection.state.collect { state ->
                if (state.isLoading || state.isConnected) cancelQrExpiryTimer()
            }
        }

    }

    fun reconnectToDevice(device: WifiAdbDevice) {
        wifiAdbRepository.reconnect(
            device,
            object : WifiAdbRepositoryImpl.ReconnectListener {
                override fun onReconnectSuccess() {
                    WifiAdbConnection.setCurrentDevice(device)
                    _lastConnectedDevice.value = currentDevice.value
                    // State is already set by repository
                }

                override fun onReconnectFailed(requiresPairing: Boolean) {
                    WifiAdbConnection.setCurrentDevice(null)
                }
            }
        )
    }

    fun reconnectToDeviceWithCallback(
        device: WifiAdbDevice,
        onSuccess: () -> Unit,
        onFailure: (requiresPairing: Boolean) -> Unit
    ) {
        wifiAdbRepository.reconnect(
            device,
            object : WifiAdbRepositoryImpl.ReconnectListener {
                override fun onReconnectSuccess() {
                    WifiAdbConnection.setCurrentDevice(device)
                    _lastConnectedDevice.value = currentDevice.value
                    // State is already set by repository
                    // Ensure callback runs on main thread for Toast
                    viewModelScope.launch {
                        onSuccess()
                    }
                }

                override fun onReconnectFailed(requiresPairing: Boolean) {
                    WifiAdbConnection.setCurrentDevice(null)
                    // Ensure callback runs on main thread for Toast
                    viewModelScope.launch {
                        onFailure(requiresPairing)
                    }
                }
            }
        )
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

    /**
     * Builds the QR bitmap for the current pairing code, skipping the work when that code already
     * has one. The pager recreates the QR tab on every swipe, so this is called far more often than
     * the code actually changes.
     */
    fun ensureQrGenerated() {
        if (generatedQrForCode == _pairingCode.value && _qrBitmap.value != null) return
        qrGenerationJob?.cancel()
        qrGenerationJob = viewModelScope.launch { generateQrForCurrentCode() }
    }

    private suspend fun generateQrForCurrentCode() {
        val code = _pairingCode.value
        try {
            _qrBitmap.value = wifiAdbRepository.generatePairingQR(
                sessionId = QR_SESSION_ID,
                pairingCode = code
            )
            generatedQrForCode = code
        } catch (e: Exception) {
            Log.e(QR_GENERATION_ERROR_TAG, "Failed to generate QR", e)
        }
    }

    /**
     * Makes [mode] the only running scan.
     *
     * Returns early when the mode is unchanged so that recomposition, which happens on every swipe,
     * cannot tear down and restart a scan that is already running.
     */
    fun setDiscoveryMode(mode: PairingDiscoveryMode) {
        if (discoveryMode == mode) return
        stopDiscovery(discoveryMode)
        discoveryMode = mode
        startDiscovery(mode)
    }

    private fun startDiscovery(mode: PairingDiscoveryMode) = when (mode) {
        PairingDiscoveryMode.Qr -> startQrPairDiscovery(_pairingCode.value)
        PairingDiscoveryMode.Code -> startCodePairingDiscovery()
        PairingDiscoveryMode.None -> Unit
    }

    private fun stopDiscovery(mode: PairingDiscoveryMode) = when (mode) {
        PairingDiscoveryMode.Qr -> stopQrPairDiscovery()
        PairingDiscoveryMode.Code -> stopCodePairingDiscovery()
        PairingDiscoveryMode.None -> Unit
    }

    /**
     * Issues a fresh pairing code and restarts the QR scan behind it.
     *
     * Used by the retry control on an expired code, and after a failed pairing attempt, which
     * consumes the code it was given.
     */
    fun refreshQrPairing() {
        stopDiscovery(discoveryMode)
        discoveryMode = PairingDiscoveryMode.None
        qrGenerationJob?.cancel()
        qrGenerationJob = viewModelScope.launch {
            _pairingCode.value = generatePairingCode()
            generateQrForCurrentCode()
            _isQrExpired.value = false
            setDiscoveryMode(PairingDiscoveryMode.Qr)
        }
    }

    private fun startQrExpiryTimer() {
        qrExpiryJob?.cancel()
        qrExpiryJob = viewModelScope.launch {
            delay(QR_PAIRING_TIMEOUT_MS)
            expireQrPairing()
        }
    }

    private fun expireQrPairing() {
        if (discoveryMode != PairingDiscoveryMode.Qr) return
        qrExpiryJob = null
        wifiAdbRepository.stopMdnsDiscovery()
        discoveryMode = PairingDiscoveryMode.None
        _isQrExpired.value = true
    }

    private fun cancelQrExpiryTimer() {
        qrExpiryJob?.cancel()
        qrExpiryJob = null
    }

    private fun generatePairingCode(): String =
        (PAIRING_CODE_MIN + SecureRandom().nextInt(PAIRING_CODE_RANGE)).toString()

    private fun startQrPairDiscovery(pairingCode: String) {
        if (pairingCode.length != PAIRING_CODE_LENGTH) return
        _isQrExpired.value = false
        startQrExpiryTimer()

        wifiAdbRepository.pairingWithQr(
            pairingCode,
            autoPair = true,
            callback = object :
                WifiAdbRepositoryImpl.MdnsDiscoveryCallback {
                override fun onServiceFound(name: String, ip: String, port: Int) {
                    WifiAdbConnection.updateState(WifiAdbState.Pairing())
                }

                override fun onPairingSuccess(ip: String, port: Int) {
                    // State is already set to Connected by the repository
                    // Do not overwrite it here - this was causing the success dialog to not appear
                }

                override fun onPairingFailed(ip: String, port: Int) {
                    // Event is emitted by repository
                }

                override fun onServiceLost(name: String) {
                    Log.d("ADB", "Service lost: $name")
                }

                override fun onError(e: Throwable) {
                    Log.e("ADB", "Error: ${e.message}")
                }
            }
        )
    }

    private fun stopQrPairDiscovery() {
        cancelQrExpiryTimer()
        wifiAdbRepository.stopMdnsDiscovery()
    }

    /**
     * Start discovery for both pairing and connect services.
     * Used when "Pair Using Code" tab is opened.
     */
    private fun startCodePairingDiscovery() {
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
    private fun stopCodePairingDiscovery() {
        wifiAdbRepository.stopCodePairingDiscovery()
        _discoveredPairingServices.value = emptyList()
    }

    /**
     * Pair with a discovered device using the entered pairing code.
     * Uses cached connect port for immediate connection after pairing.
     */
    fun pairWithCode(service: DiscoveredPairingService, pairingCode: String) {
        if (pairingCode.length != 6) return

        WifiAdbConnection.updateState(WifiAdbState.Pairing())

        disconnect()

        wifiAdbRepository.pairAndConnect(
            ip = service.ip,
            pairingPort = service.port,
            pairingCode = pairingCode,
            callback = object : WifiAdbRepositoryImpl.MdnsDiscoveryCallback {
                override fun onPairingSuccess(ip: String, port: Int) {
                    // State is already set by repository
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

    override fun onCleared() {
        super.onCleared()
        cancelQrExpiryTimer()
        stopDiscovery(discoveryMode)
        discoveryMode = PairingDiscoveryMode.None
    }
}
