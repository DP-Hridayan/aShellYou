package `in`.hridayan.ashell.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.Stable
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.shell.common.domain.repository.ShellRepository
import javax.inject.Inject

@Stable
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
}
