package `in`.hridayan.ashell.qstiles.presentation.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.qstiles.data.repository.TileRepositoryImpl
import `in`.hridayan.ashell.qstiles.domain.executor.TileExecutionManager
import `in`.hridayan.ashell.qstiles.domain.model.RunningTileState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import `in`.hridayan.ashell.qstiles.presentation.model.TileDashBoardState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TileDashboardViewModel @Inject constructor(
    private val repository: TileRepository,
    private val executionManager: TileExecutionManager
) : ViewModel() {

    val state: StateFlow<TileDashBoardState> =
        combine(
            repository.getTiles().distinctUntilChanged(),
            executionManager.runningTileStates
        ) { tiles, running ->
            TileDashBoardState(
                tiles = tiles.filter { it.isCustom },
                activeCount = tiles.count { it.isActive },
                runningTiles = running
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                TileDashBoardState()
            )

    /**
     * Returns a [kotlinx.coroutines.flow.Flow] that emits whether [tileId] is currently running.
     */
    fun isTileRunning(tileId: Int) = executionManager.runningTileStates
        .map { it.containsKey(tileId) }
        .distinctUntilChanged()

    /**
     * Toggles a tile on or off.
     * Maps to panel highlight and tray visibility.
     */
    fun toggleTile(tile: TileConfig) {
        viewModelScope.launch {
            repository.toggleTile(tile.id)
        }
    }
}