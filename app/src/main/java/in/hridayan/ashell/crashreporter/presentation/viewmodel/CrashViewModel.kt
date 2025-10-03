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

    private val _crashLogs = mutableStateOf<List<CrashReport>>(emptyList())
    val crashLogs: State<List<CrashReport>> = _crashLogs

    private val _latestCrash = mutableStateOf<CrashReport?>(null)
    val latestCrash: State<CrashReport?> = _latestCrash

    init {
        loadCrashes()
        loadLatestCrash()
    }

    fun loadCrashes() {
        viewModelScope.launch {
            _crashLogs.value = crashRepository.getAllCrashes()
        }
    }

    fun loadLatestCrash() {
        viewModelScope.launch {
            _latestCrash.value = crashRepository.getLatestCrash()
        }
    }

    fun addCrash(crash: CrashReport) {
        viewModelScope.launch {
            crashRepository.addCrash(crash)
            loadCrashes()
            loadLatestCrash()
        }
    }

    fun clearCrashes() {
        viewModelScope.launch {
            crashRepository.clearAllCrashes()
            loadCrashes()
            _latestCrash.value = null
        }
    }
}
