package `in`.hridayan.ashell.home.presentation.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.domain.repository.ShellRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shellRepository: ShellRepository
) : ViewModel() {

    fun reboot(cmd: Array<String>) {
        viewModelScope.launch {
            shellRepository.executeBasicCommand(cmd.joinToString(" ")).collect {}
        }
    }
}
