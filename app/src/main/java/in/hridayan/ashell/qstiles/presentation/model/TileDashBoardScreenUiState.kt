package `in`.hridayan.ashell.qstiles.presentation.model

import `in`.hridayan.ashell.qstiles.domain.model.RunningTileState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.model.TileLog

data class TileDashBoardScreenUiState(
    val tiles: List<TileConfig> = emptyList(),
    val activeCount: Int = 0,
    val runningTiles: Map<Int, RunningTileState> = emptyMap(),
    
    // ── Logs & Navigation ────────────────────────────────────────────────────
    val currentTab: Int = 0, // 0 = Dashboard, 1 = Logs
    val logs: List<TileLog> = emptyList(),
    val totalExecutions: Int = 0,
    val successRate: String = "0.0%",
    val logsSearchQuery: String = ""
)