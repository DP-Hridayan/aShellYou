package `in`.hridayan.ashell.shell.otg_adb_shell.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.otg_adb_shell.data.repository.OtgRepositoryImpl
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.model.OtgState
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.repository.OtgRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtgViewModel @Inject constructor(
    private val repository: OtgRepository
) : ViewModel() {

    val state: StateFlow<OtgState> = repository.state

    fun startScan() = viewModelScope.launch {
        repository.searchDevices()
    }

    fun disconnect() = viewModelScope.launch {
        repository.disconnect()
    }

    fun unRegister() = viewModelScope.launch {
        repository.unRegister()
    }


    override fun onCleared() {
        if (repository is OtgRepositoryImpl) {
            unRegister()
        }
        super.onCleared()
    }
}
