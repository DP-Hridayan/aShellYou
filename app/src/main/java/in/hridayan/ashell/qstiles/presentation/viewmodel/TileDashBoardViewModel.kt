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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TileDashboardState(
    val tiles: List<TileConfig> = emptyList(),
    val activeCount: Int = 0,
    val runningTiles: Map<Int, RunningTileState> = emptyMap()
)

@HiltViewModel
class TileDashboardViewModel @Inject constructor(
    private val repository: TileRepository,
    private val repositoryImpl: TileRepositoryImpl,
    private val executionManager: TileExecutionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val state: StateFlow<TileDashboardState> =
        combine(
            repository.getTiles().distinctUntilChanged(),
            executionManager.runningTileStates
        ) { tiles, running ->
            TileDashboardState(
                tiles = tiles.filter { it.isCustom },
                activeCount = tiles.count { it.isActive },
                runningTiles = running
            )
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                TileDashboardState()
            )

    /**
     * Returns a [kotlinx.coroutines.flow.Flow] that emits whether [tileId] is currently running.
     */
    fun isTileRunning(tileId: Int) = executionManager.runningTileStates
        .map { it.containsKey(tileId) }
        .distinctUntilChanged()

    /**
     * Toggles a tile on or off.
     *
     * Activation: assigns the lowest free slot (0–9) atomically.
     *             Shows a Toast if all 10 slots are occupied.
     *
     * Deactivation: frees its slot. The corresponding QS tile
     *               will transition to STATE_UNAVAILABLE automatically.
     */
    fun toggleTile(tile: TileConfig) {
        viewModelScope.launch {
            if (tile.isActive) {
                // Deactivate — always allowed
                repositoryImpl.deactivateTile(tile)
            } else {
                // Activate — subject to slot availability
                val updated = repositoryImpl.activateTile(tile)
                if (updated == null) {
                    Toast.makeText(
                        context,
                        "Maximum 10 active tiles allowed. Deactivate another tile first.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}