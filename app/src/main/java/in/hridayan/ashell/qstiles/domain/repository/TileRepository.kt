package `in`.hridayan.ashell.qstiles.domain.repository

import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import kotlinx.coroutines.flow.Flow

interface TileRepository {
    suspend fun createTile(config: TileConfig)

    suspend fun updateTile(config: TileConfig)

    suspend fun deleteTile(id: Int)

    fun getTiles(): Flow<List<TileConfig>>

    fun getTile(id: Int): Flow<TileConfig?>

    suspend fun getTileOnce(id: Int): TileConfig?

    fun getActiveTileCount(): Flow<Int>

    suspend fun getFirstAvailableTileId(): Int?

    suspend fun isTileSlotAvailable(id: Int): Boolean
}