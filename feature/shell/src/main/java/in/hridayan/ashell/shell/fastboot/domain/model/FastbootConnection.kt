package `in`.hridayan.ashell.shell.fastboot.domain.model

import `in`.hridayan.ashell.core.domain.model.FastbootState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object FastbootConnection {
    private val _state = MutableStateFlow<FastbootState>(FastbootState.Idle)
    val state = _state.asStateFlow()

    fun updateState(newState: FastbootState) {
        _state.value = newState
    }

    val currentState: FastbootState get() = _state.value
}
