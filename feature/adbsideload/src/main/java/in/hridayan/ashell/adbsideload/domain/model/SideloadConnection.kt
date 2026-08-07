package `in`.hridayan.ashell.adbsideload.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SideloadConnection {
    private val _state = MutableStateFlow<SideloadState>(SideloadState.Idle)
    val state = _state.asStateFlow()

    fun updateState(newState: SideloadState) {
        _state.value = newState
    }

    val currentState: SideloadState get() = _state.value
}
