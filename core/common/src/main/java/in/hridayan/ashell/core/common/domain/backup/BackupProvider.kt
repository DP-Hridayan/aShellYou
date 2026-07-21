package `in`.hridayan.ashell.core.common.domain.backup

import kotlinx.serialization.json.JsonElement

interface BackupProvider {
    val featureId: String

    suspend fun getBackupData(): JsonElement?

    suspend fun restoreData(data: JsonElement?, legacyData: (String) -> JsonElement?)
}