package `in`.hridayan.ashell.qstiles.presentation.model

import `in`.hridayan.ashell.qstiles.domain.model.TileLog

data class TileLogsState(
    val logs: List<TileLog> = emptyList(),
    val totalExecutions: Int = 0,
    val successRate: Float = 0f
)