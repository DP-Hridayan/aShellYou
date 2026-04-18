package `in`.hridayan.ashell.qstiles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.qstiles.domain.executor.TileExecutionManager
import `in`.hridayan.ashell.qstiles.domain.model.RunningTileState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.model.TileLog
import `in`.hridayan.ashell.qstiles.domain.repository.TileLogRepository
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import `in`.hridayan.ashell.qstiles.presentation.model.TileDashBoardScreenUiState
import `in`.hridayan.ashell.qstiles.presentation.screen.TileScreenTabs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TileDashboardViewModel @Inject constructor(
    private val repository: TileRepository,
    private val executionManager: TileExecutionManager,
    private val logRepository: TileLogRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(TileScreenTabs.TILES)
    private val _logsSearchQuery = MutableStateFlow("")

    val state: StateFlow<TileDashBoardScreenUiState> =
        combine(
            repository.getTiles().distinctUntilChanged(),
            executionManager.runningTileStates,
            combine(
                logRepository.getAllLogs().distinctUntilChanged(),
                logRepository.getTotalExecutions().distinctUntilChanged(),
                logRepository.getSuccessCount().distinctUntilChanged(),
                _currentTab,
                _logsSearchQuery
            ) { logs, total, success, tab, query ->
                DataBundle(logs, total, success, tab, query)
            }
        ) { tiles, running, bundle ->
            
            val logs = bundle.logs
            val total = bundle.total
            val success = bundle.success
            val tab = bundle.tab
            val query = bundle.query

            val filteredLogs = if (query.isBlank()) {
                logs
            } else {
                logs.filter { log ->
                    val tileName = tiles.find { it.id == log.tileId }?.name ?: "Unknown"
                    tileName.contains(query, ignoreCase = true)
                }
            }

            val successRateStr = if (total > 0) {
                String.format(Locale.getDefault(), "%.1f%%", (success.toFloat() / total.toFloat()) * 100)
            } else {
                "0.0%"
            }

            TileDashBoardScreenUiState(
                tiles = tiles.filter { it.isCustom },
                activeCount = tiles.count { it.activeState.isActive },
                runningTiles = running,
                currentTab = tab,
                logs = filteredLogs,
                totalExecutions = total,
                successRate = successRateStr,
                logsSearchQuery = query
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                TileDashBoardScreenUiState()
            )

    fun onTabChange(index: Int) {
        _currentTab.value = index
    }

    fun onLogsSearchQueryChange(query: String) {
        _logsSearchQuery.value = query
    }

    private data class DataBundle(
        val logs: List<TileLog>,
        val total: Int,
        val success: Int,
        val tab: Int,
        val query: String
    )
}