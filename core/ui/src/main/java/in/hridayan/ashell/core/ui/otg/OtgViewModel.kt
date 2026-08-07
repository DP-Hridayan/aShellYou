package `in`.hridayan.ashell.core.ui.otg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgConnection
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.repository.OtgRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtgViewModel @Inject constructor(
    private val repository: OtgRepository
) : ViewModel() {

    val state: StateFlow<OtgState> = OtgConnection.state

    fun startScan() = viewModelScope.launch {
        repository.searchDevices()
    }

    fun disconnect() = viewModelScope.launch {
        repository.disconnect()
    }

    fun unRegister() = viewModelScope.launch {
        repository.unRegister()
    }

    /**
     * Reboot the connected OTG ADB device into bootloader/fastboot mode.
     * Uses ADB protocol's native "reboot:bootloader" service.
     */
    fun rebootToBootloader() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val connection = repository.getAdbConnection() ?: return@launch
            connection.open("reboot:bootloader")
        } catch (_: Exception) {
        }
    }

    override fun onCleared() {
        unRegister()
        super.onCleared()
    }
}
