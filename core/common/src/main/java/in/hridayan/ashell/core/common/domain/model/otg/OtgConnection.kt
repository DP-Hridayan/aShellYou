package `in`.hridayan.ashell.core.common.domain.model.otg

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object OtgConnection {
    private val _state = MutableStateFlow<OtgState>(OtgState.Idle)
    val state = _state.asStateFlow()

    fun updateState(newState: OtgState) {
        _state.value = newState
    }

    val currentState: OtgState get() = _state.value
}
