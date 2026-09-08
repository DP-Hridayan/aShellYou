package `in`.hridayan.ashell.core.presentation.theme.data.backup

import `in`.hridayan.ashell.core.common.domain.provider.BackupProvider
import `in`.hridayan.ashell.core.presentation.theme.data.CustomColorSchemeDao
import `in`.hridayan.ashell.core.presentation.theme.data.UserGeneratedColorSchemeEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import javax.inject.Inject

class ColorSchemeBackupProvider @Inject constructor(
    private val dao: CustomColorSchemeDao,
    private val json: Json
) : BackupProvider {

    override val featureId: String = "color_schemes"

    override suspend fun getBackupData(): JsonElement? {
        val schemes = dao.getAllColorSchemesOnce()
        if (schemes.isEmpty()) return null
        return json.encodeToJsonElement(schemes)
    }

    override suspend fun restoreData(data: JsonElement?, legacyData: (String) -> JsonElement?) {
        val jsonData = data ?: legacyData("color_schemes") ?: return

        try {
            val schemes = json.decodeFromJsonElement<List<UserGeneratedColorSchemeEntity>>(jsonData)
            if (schemes.isNotEmpty()) {
                dao.deleteAllColorSchemes()
                schemes.forEach { scheme ->
                    dao.insertColorScheme(scheme)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
