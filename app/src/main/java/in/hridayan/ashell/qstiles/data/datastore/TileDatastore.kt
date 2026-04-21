package `in`.hridayan.ashell.qstiles.data.datastore

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.qstiles.data.provider.tileDataStore
import `in`.hridayan.ashell.qstiles.domain.model.TileActiveState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * DataStore-backed persistence layer for [TileConfig].
 *
 * Key-naming convention: `tile_<id>_<field>`.
 * All [TileActiveState] fields are persisted with the prefix `as_` to avoid collisions.
 */
class TileDatastore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val ds = context.tileDataStore

    companion object {
        private val TILE_IDS = stringSetPreferencesKey("tile_ids")
    }

    private fun keyStr(id: Int, field: String) = stringPreferencesKey("tile_${id}_$field")
    private fun keyInt(id: Int, field: String) = intPreferencesKey("tile_${id}_$field")
    private fun keyBool(id: Int, field: String) = booleanPreferencesKey("tile_${id}_$field")
    private fun keyLong(id: Int, field: String) = longPreferencesKey("tile_${id}_$field")

    private fun buildActiveState(
        id: Int,
        prefs: Preferences,
    ): TileActiveState = TileActiveState(
        isToggleable = prefs[keyBool(id, "as_toggleable")] ?: false,
        isActive = prefs[keyBool(id, "as_active")] ?: false,
        activeTileSubtitle = TextFieldValue(prefs[keyStr(id, "as_on_subtitle")] ?: "On"),
        inactiveTileSubtitle = TextFieldValue(prefs[keyStr(id, "as_off_subtitle")] ?: "Off"),
        activeCommand = TextFieldValue(prefs[keyStr(id, "as_on_cmd")] ?: ""),
        inactiveCommand = TextFieldValue(prefs[keyStr(id, "as_off_cmd")] ?: ""),
    )

    private fun buildTileConfig(
        id: Int,
        prefs: Preferences,
    ): TileConfig? {
        val name = prefs[keyStr(id, "name")] ?: return null
        return TileConfig(
            id = id,
            name = name,
            iconId = prefs[keyStr(id, "icon")] ?: "terminal",
            executionMode = prefs[keyInt(id, "mode")] ?: 0,
            isCustom = prefs[keyBool(id, "custom")] ?: false,
            slotIndex = prefs[keyInt(id, "slot")].let { if (it == null || it == -1) null else it },
            timeoutMs = prefs[keyLong(
                id,
                "timeout"
            )].let { if (it == null || it == -1L) null else it },
            activeState = buildActiveState(id, prefs),
        )
    }

    fun getTile(tileId: Int): Flow<TileConfig?> =
        ds.data.map { buildTileConfig(tileId, it) }

    suspend fun getTileOnce(tileId: Int): TileConfig? =
        buildTileConfig(tileId, ds.data.first())

    fun getAllTiles(): Flow<List<TileConfig>> = ds.data.map { prefs ->
        val ids = prefs[TILE_IDS]?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        ids.mapNotNull { buildTileConfig(it, prefs) }
    }

    suspend fun saveTile(config: TileConfig) {
        ds.edit { prefs ->
            // Register in the ID index
            prefs[TILE_IDS] = (prefs[TILE_IDS] ?: emptySet()) + config.id.toString()

            // Core fields
            prefs[keyStr(config.id, "name")] = config.name
            prefs[keyInt(config.id, "mode")] = config.executionMode
            prefs[keyStr(config.id, "icon")] = config.iconId
            prefs[keyBool(config.id, "custom")] = config.isCustom
            prefs[keyInt(config.id, "slot")] = config.slotIndex ?: -1
            prefs[keyLong(config.id, "timeout")] = config.timeoutMs ?: -1L

            // TileActiveState fields
            with(config.activeState) {
                prefs[keyBool(config.id, "as_toggleable")] = isToggleable
                prefs[keyBool(config.id, "as_active")] = isActive
                prefs[keyStr(config.id, "as_on_subtitle")] = activeTileSubtitle.text
                prefs[keyStr(config.id, "as_off_subtitle")] = inactiveTileSubtitle.text
                prefs[keyStr(config.id, "as_on_cmd")] = activeCommand.text
                prefs[keyStr(config.id, "as_off_cmd")] = inactiveCommand.text
            }
        }
    }

    suspend fun clearTile(tileId: Int) {
        ds.edit { prefs ->
            prefs[TILE_IDS] = (prefs[TILE_IDS] ?: emptySet()) - tileId.toString()

            prefs.remove(keyStr(tileId, "name"))
            prefs.remove(keyInt(tileId, "mode"))
            prefs.remove(keyStr(tileId, "icon"))
            prefs.remove(keyBool(tileId, "custom"))
            prefs.remove(keyInt(tileId, "slot"))
            prefs.remove(keyLong(tileId, "timeout"))
            // TileActiveState
            prefs.remove(keyBool(tileId, "as_toggleable"))
            prefs.remove(keyBool(tileId, "as_active"))
            prefs.remove(keyStr(tileId, "as_on_subtitle"))
            prefs.remove(keyStr(tileId, "as_off_subtitle"))
            prefs.remove(keyStr(tileId, "as_on_cmd"))
            prefs.remove(keyStr(tileId, "as_off_cmd"))
        }
    }

    /** Counts tiles that are currently in the ON state (used by dashboard). */
    fun getActiveTileCount(): Flow<Int> = ds.data.map { prefs ->
        val ids = prefs[TILE_IDS]?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        ids.count { prefs[keyBool(it, "as_active")] == true }
    }

    suspend fun getAllTilesOnce(): List<TileConfig> = ds.data.map { prefs ->
        val ids = prefs[TILE_IDS]?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        ids.mapNotNull { buildTileConfig(it, prefs) }
    }.first()

    suspend fun deleteAllTiles() {
        ds.edit { prefs ->
            val ids = prefs[TILE_IDS]?.mapNotNull { it.toIntOrNull() } ?: emptyList()
            ids.forEach { id ->
                prefs.remove(keyStr(id, "name"))
                prefs.remove(keyInt(id, "mode"))
                prefs.remove(keyStr(id, "icon"))
                prefs.remove(keyBool(id, "custom"))
                prefs.remove(keyInt(id, "slot"))
                prefs.remove(keyLong(id, "timeout"))
                prefs.remove(keyBool(id, "as_toggleable"))
                prefs.remove(keyBool(id, "as_active"))
                prefs.remove(keyStr(id, "as_on_subtitle"))
                prefs.remove(keyStr(id, "as_off_subtitle"))
                prefs.remove(keyStr(id, "as_on_cmd"))
                prefs.remove(keyStr(id, "as_off_cmd"))
            }
            prefs.remove(TILE_IDS)
        }
    }

    suspend fun saveAllTiles(tiles: List<TileConfig>) {
        tiles.forEach { saveTile(it) }
    }
}