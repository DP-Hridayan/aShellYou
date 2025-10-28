package `in`.hridayan.ashell.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.domain.repository.ShellRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shellRepository: ShellRepository
) : ViewModel() {
    fun requestRootAccess(): Boolean = shellRepository.hasRootAccess()
}
