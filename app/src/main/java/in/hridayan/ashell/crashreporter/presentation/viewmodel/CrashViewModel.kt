package `in`.hridayan.ashell.crashreporter.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.crashreporter.domain.model.CrashReport
import `in`.hridayan.ashell.crashreporter.domain.repository.CrashRepository
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CrashViewModel @Inject constructor(
    private val crashRepository: CrashRepository
) : ViewModel() {
    val crashLogs = crashRepository.getAllCrashes()

    private val _latestCrash = mutableStateOf<CrashReport?>(null)
    val latestCrash: State<CrashReport?> = _latestCrash

    private val _crash = mutableStateOf<CrashReport?>(null)
    val crash: State<CrashReport?> = _crash

    init {
        loadLatestCrash()
    }

    fun setViewingCrash(crashReport: CrashReport) {
        _crash.value = crashReport
    }

    fun addCrash(crash: CrashReport) {
        viewModelScope.launch {
            crashRepository.addCrash(crash)
            loadLatestCrash()
        }
    }

    fun clearCrashes() {
        viewModelScope.launch {
            crashRepository.clearAllCrashes()
            _latestCrash.value = null
        }
    }

    private fun loadLatestCrash() {
        viewModelScope.launch {
            _latestCrash.value = crashRepository.getLatestCrash()
        }
    }
}
