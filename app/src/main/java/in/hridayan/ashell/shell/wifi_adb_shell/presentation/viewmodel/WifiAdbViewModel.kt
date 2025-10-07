package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.data.adb.WifiAdbShell
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WifiAdbViewModel @Inject constructor() : ViewModel() {
    private val executor = WifiAdbShell()

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
    private val _state = mutableStateOf<WifiAdbState?>(null)
    val state: State<WifiAdbState?> = _state

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

    fun startPairing() {
        checkInputFieldValidity()

        if (pairingFieldsInvalid) {
            _state.value = WifiAdbState.PairingFailed("Invalid fields")
            return
        }
        val ip = _ipAddress.value
        val port = _pairingPort.value.toIntOrNull() ?: return
        val code = _pairingCode.value.toIntOrNull() ?: return

        _state.value = WifiAdbState.PairingStarted()

        executor.pair(ip, port, code, object : WifiAdbShell.PairingListener {
            override fun onPairingSuccess() {
                _state.value = WifiAdbState.PairingSuccess("Paired successfully")
            }

            override fun onPairingFailed() {
                _state.value = WifiAdbState.PairingFailed("Pairing failed")
            }
        })
    }

    fun startConnecting() {
        checkInputFieldValidity()

        if (connectFieldsInvalid) {
            _state.value = WifiAdbState.ConnectFailed("Invalid fields")
            return
        }
        val ip = _ipAddress.value
        val port = _connectPort.value.toIntOrNull() ?: return

        _state.value = WifiAdbState.ConnectStarted()

        executor.connect(ip, port, object : WifiAdbShell.ConnectionListener {
            override fun onConnectionSuccess() {
                _state.value = WifiAdbState.ConnectSuccess("Connected!")
            }

            override fun onConnectionFailed() {
                _state.value = WifiAdbState.ConnectFailed("Connection failed")
            }
        })
    }

    fun startPairingFlow(sessionId: String, pairingCode: Int) {
        executor.waitForPairRequest(sessionId, pairingCode, object : WifiAdbShell.StateCallback {
            override fun onStateChanged(state: WifiAdbState) {
                viewModelScope.launch {
                    _state.value = state
                }
            }
        })
    }

    fun refreshFieldsForNewPair() {
        _state.value = WifiAdbState.None
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