package `in`.hridayan.ashell.qstiles.domain.repository

import kotlinx.coroutines.flow.Flow
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig

interface TileRepository {
    fun getTile(id: Int): Flow<TileConfig?>
    suspend fun saveTile(config: TileConfig)
    suspend fun clearTile(id: Int)
}