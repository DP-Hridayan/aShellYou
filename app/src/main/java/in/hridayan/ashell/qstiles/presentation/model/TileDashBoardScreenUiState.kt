package `in`.hridayan.ashell.qstiles.presentation.model

import `in`.hridayan.ashell.qstiles.domain.model.RunningTileState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig

data class TileDashBoardScreenUiState(
    val tiles: List<TileConfig> = emptyList(),
    val activeCount: Int = 0,
    val runningTiles: Map<Int, RunningTileState> = emptyMap()
)