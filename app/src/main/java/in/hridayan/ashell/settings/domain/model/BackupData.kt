package `in`.hridayan.ashell.settings.domain.model

import `in`.hridayan.ashell.commandexamples.data.local.model.CommandEntity
import `in`.hridayan.ashell.qstiles.data.model.TileLogEntity
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.shell.common.data.model.BookmarkEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val settings: Map<String, String?>? = null,
    val commands: List<CommandEntity>? = null,
    val bookmarks: List<BookmarkEntity>? = null,
    val tiles: List<TileConfig>? = null,
    val tileLogs: List<TileLogEntity>? = null,
    val backupTime: String,
    val backupType: String,
    val backupMode: String
)
