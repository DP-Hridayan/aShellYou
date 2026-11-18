package `in`.hridayan.ashell.qstiles.tool

import `in`.hridayan.ashell.core.common.domain.model.TileExecutionMode
import `in`.hridayan.ashell.qstiles.domain.model.TileActiveState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class GetQsTileSlotsToolTest {

    @Test
    fun execute_displaysTcpIpExecutionModeCorrectly() = runTest {
        val fakeRepo = FakeTileRepository(
            listOf(
                TileConfig(
                    id = 1,
                    name = "WiFi Mode",
                    iconId = "terminal",
                    executionMode = TileExecutionMode.TCPIP,
                    activeState = TileActiveState(
                        isToggleable = false
                    ),
                    isCustom = true
                )
            )
        )
        val tool = GetQsTileSlotsTool(fakeRepo)

        val result = tool.execute(null)

        assertTrue(result.contains("[Mode: TCP/IP]"))
        assertTrue(result.contains("Slot 1: 'WiFi Mode'"))
    }

    @Test
    fun execute_displaysShizukuAndRootModesCorrectly() = runTest {
        val fakeRepo = FakeTileRepository(
            listOf(
                TileConfig(
                    id = 1,
                    name = "Shizuku Tile",
                    iconId = "terminal",
                    executionMode = TileExecutionMode.SHIZUKU,
                    activeState = TileActiveState(isToggleable = false),
                    isCustom = true
                ),
                TileConfig(
                    id = 2,
                    name = "Root Tile",
                    iconId = "terminal",
                    executionMode = TileExecutionMode.ROOT,
                    activeState = TileActiveState(isToggleable = false),
                    isCustom = true
                )
            )
        )
        val tool = GetQsTileSlotsTool(fakeRepo)

        val result = tool.execute(null)

        assertTrue(result.contains("[Mode: Shizuku]"))
        assertTrue(result.contains("[Mode: Root]"))
    }
}

private class FakeTileRepository(
    private val tiles: List<TileConfig> = emptyList()
) : TileRepository {
    override suspend fun createTile(config: TileConfig): Int? = null
    override suspend fun updateTile(config: TileConfig) {}
    override suspend fun deleteTile(id: Int) {}
    override fun getTiles(): Flow<List<TileConfig>> = flowOf(tiles)
    override fun getTile(id: Int): Flow<TileConfig?> = flowOf(tiles.firstOrNull { it.id == id })
    override suspend fun getTileOnce(id: Int): TileConfig? = tiles.firstOrNull { it.id == id }
    override fun getActiveTileCount(): Flow<Int> = flowOf(tiles.size)
    override suspend fun toggleTile(id: Int) {}
    override fun getTileBySlot(slotIndex: Int): Flow<TileConfig?> = flowOf(null)
    override suspend fun getActiveTiles(): List<TileConfig> = tiles
}
