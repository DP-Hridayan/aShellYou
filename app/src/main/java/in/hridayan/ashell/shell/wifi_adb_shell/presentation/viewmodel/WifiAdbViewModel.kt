package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbConnection
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _savedDevices = MutableStateFlow<List<WifiAdbDevice>>(emptyList())
    val savedDevices = _savedDevices.asStateFlow()

    fun loadSavedDevices() {
        _savedDevices.value = wifiAdbRepository.getSavedDevices()
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
        val code = _pairingCode.value.toIntOrNull() ?: return

        startPairing(ip, port, code)
    }

    private fun startPairing(ip: String, port: Int, code: Int) {

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

    fun startMdnsPairing(pairingCode: Int) {
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

    fun stopMdnsDiscovery(){
        wifiAdbRepository.stopMdnsDiscovery()
    }

    fun startConnectingManually() {
        checkInputFieldValidity()

        if (connectFieldsInvalid) {
            WifiAdbConnection.updateState(WifiAdbState.ConnectFailed("Invalid fields"))
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
                WifiAdbConnection.updateState(WifiAdbState.ConnectFailed("Connection failed"))
            }
        })
    }


    fun refreshFieldsForNewPair() {
        WifiAdbConnection.updateState(WifiAdbState.None)
        _ipAddress.value = ""
        _pairingPort.value = ""
        _pairingCode.value = ""
        _connectPort.value = ""
        _ipAddressError.value = false
        _pairingPortError.value = false
        _pairingCodeError.value = false
        _connectPortError.value = false
    }

    private fun checkInputFieldValidity() {
        _ipAddressError.value = _ipAddress.value.isEmpty()
        _pairingPortError.value = _pairingPort.value.isEmpty()
        _pairingCodeError.value = _pairingCode.value.isEmpty()
        _connectPortError.value = _connectPort.value.isEmpty()
    }

    private val pairingFieldsInvalid =
        _ipAddressError.value || _pairingPortError.value || _pairingCodeError.value

    private val connectFieldsInvalid =
        _ipAddressError.value || _connectPortError.value


}