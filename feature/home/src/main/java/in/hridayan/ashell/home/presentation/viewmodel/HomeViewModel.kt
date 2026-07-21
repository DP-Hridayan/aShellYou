package `in`.hridayan.ashell.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.Stable
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.core.domain.repository.ShellRepository
import javax.inject.Inject
import kotlinx.coroutines.launch

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
