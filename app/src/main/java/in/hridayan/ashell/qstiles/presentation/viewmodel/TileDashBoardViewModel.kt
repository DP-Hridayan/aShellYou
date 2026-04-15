package `in`.hridayan.ashell.qstiles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TileDashboardState(
    val tiles: List<TileConfig> = emptyList(),
    val activeCount: Int = 0
)

@HiltViewModel
class TileDashboardViewModel @Inject constructor(
    private val repository: TileRepository
) : ViewModel() {

    val state: StateFlow<TileDashboardState> =
        repository.getTiles()
            .map { tiles ->
                TileDashboardState(
                    tiles = tiles.filter { it.isCustom },
                    activeCount = tiles.count { it.isActive }
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                TileDashboardState()
            )

    fun toggleTile(tile: TileConfig) {
        viewModelScope.launch {
            repository.updateTile(
                tile.copy(isActive = !tile.isActive)
            )
        }
    }
}