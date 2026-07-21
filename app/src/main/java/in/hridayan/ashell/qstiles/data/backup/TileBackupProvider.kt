package `in`.hridayan.ashell.qstiles.data.backup

import `in`.hridayan.ashell.qstiles.data.database.TileLogDatabase
import `in`.hridayan.ashell.qstiles.data.datastore.TileDatastore
import `in`.hridayan.ashell.qstiles.data.model.TileLogEntity
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.core.common.domain.backup.BackupProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import javax.inject.Inject

@Serializable
data class TileBackupModel(
    val tiles: List<TileConfig>,
    val tileLogs: List<TileLogEntity>
)

class TileBackupProvider @Inject constructor(
    private val tileDatastore: TileDatastore,
    private val tileLogDatabase: TileLogDatabase,
    private val json: Json
) : BackupProvider {

    override val featureId: String = "qstiles"

    override suspend fun getBackupData(): JsonElement? {
        val tiles = tileDatastore.getAllTilesOnce()
        val tileLogs = tileLogDatabase.dao().getAllLogsOnce()
        
        if (tiles.isEmpty() && tileLogs.isEmpty()) return null
        
        val backupModel = TileBackupModel(tiles, tileLogs)
        return json.encodeToJsonElement(backupModel)
    }

    override suspend fun restoreData(data: JsonElement?, legacyData: (String) -> JsonElement?) {
        if (data != null) {
            try {
                val backupModel = json.decodeFromJsonElement<TileBackupModel>(data)
                
                if (backupModel.tiles.isNotEmpty()) {
                    tileDatastore.deleteAllTiles()
                    tileDatastore.saveAllTiles(backupModel.tiles)
                }
                
                if (backupModel.tileLogs.isNotEmpty()) {
                    tileLogDatabase.dao().deleteAllLogs()
                    tileLogDatabase.dao().insertAll(backupModel.tileLogs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Restore legacy data
            val legacyTiles = legacyData("tiles")
            val legacyLogs = legacyData("tileLogs")
            
            try {
                if (legacyTiles != null) {
                    val tilesList = json.decodeFromJsonElement<List<TileConfig>>(legacyTiles)
                    if (tilesList.isNotEmpty()) {
                        tileDatastore.deleteAllTiles()
                        tileDatastore.saveAllTiles(tilesList)
                    }
                }
                
                if (legacyLogs != null) {
                    val logsList = json.decodeFromJsonElement<List<TileLogEntity>>(legacyLogs)
                    if (logsList.isNotEmpty()) {
                        tileLogDatabase.dao().deleteAllLogs()
                        tileLogDatabase.dao().insertAll(logsList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}