package `in`.hridayan.ashell.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.common.domain.provider.TunnelStatusProvider
import `in`.hridayan.ashell.core.common.domain.provider.TunnelStatusSummary
import `in`.hridayan.ashell.core.common.domain.repository.ShellRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shellRepository: ShellRepository,
    tunnelStatusProvider: TunnelStatusProvider
) : ViewModel() {

    val tunnelStatus: StateFlow<TunnelStatusSummary> = tunnelStatusProvider.summary

    fun reboot(cmd: Array<String>) {
        viewModelScope.launch {
            shellRepository.executeBasicCommand(cmd.joinToString(" ")).collect {}
        }
    }
}
