package `in`.hridayan.ashell.qstiles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.qstiles.domain.repository.TileLogRepository
import `in`.hridayan.ashell.qstiles.presentation.model.TileLogsState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TileLogsViewModel @Inject constructor(
    private val logRepository: TileLogRepository
) : ViewModel() {

    val state: StateFlow<TileLogsState> =
        logRepository.getAllLogs()
            .map { logs ->
                val total = logs.size
                val success = logs.count { it.isSuccess }

                TileLogsState(
                    logs = logs,
                    totalExecutions = total,
                    successRate = if (total == 0) 0f else success.toFloat() / total
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                TileLogsState()
            )
}