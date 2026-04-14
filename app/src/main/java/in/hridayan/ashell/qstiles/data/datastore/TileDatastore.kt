package `in`.hridayan.ashell.qstiles.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.qstiles.data.provider.tileDataStore
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TileDatastore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val ds = context.tileDataStore

    private fun key(id: Int, field: String) = stringPreferencesKey("tile_${id}_$field")
    private fun keyInt(id: Int, field: String) = intPreferencesKey("tile_${id}_$field")
    private fun keyBool(id: Int, field: String) = booleanPreferencesKey("tile_${id}_$field")

    fun getTile(tileId: Int): Flow<TileConfig?> {
        return ds.data.map { prefs ->
            val label = prefs[key(tileId, "label")] ?: return@map null

            TileConfig(
                tileId = tileId,
                label = label,
                command = prefs[key(tileId, "command")] ?: "",
                iconId = prefs[key(tileId, "icon")] ?: "terminal",
                executionMode = prefs[keyInt(tileId, "mode")] ?: 0,
                isEnabled = prefs[keyBool(tileId, "enabled")] ?: false
            )
        }
    }

    suspend fun saveTile(config: TileConfig) {
        ds.edit { prefs ->
            prefs[key(config.tileId, "label")] = config.label
            prefs[key(config.tileId, "command")] = config.command
            prefs[key(config.tileId, "icon")] = config.iconId
            prefs[keyInt(config.tileId, "mode")] = config.executionMode
            prefs[keyBool(config.tileId, "enabled")] = config.isEnabled
        }
    }

    suspend fun clearTile(tileId: Int) {
        ds.edit { prefs ->
            prefs.remove(key(tileId, "label"))
            prefs.remove(key(tileId, "command"))
            prefs.remove(key(tileId, "icon"))
            prefs.remove(keyInt(tileId, "mode"))
            prefs.remove(keyBool(tileId, "enabled"))
        }
    }
}