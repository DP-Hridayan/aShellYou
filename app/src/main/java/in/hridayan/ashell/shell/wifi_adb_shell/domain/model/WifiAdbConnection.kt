package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object WifiAdbConnection {
    private val _state = MutableStateFlow<WifiAdbState>(WifiAdbState.None)
    val state = _state.asStateFlow()

    fun updateState(newState: WifiAdbState) {
        _state.value = newState
    }

    val currentState: WifiAdbState get() = _state.value
}