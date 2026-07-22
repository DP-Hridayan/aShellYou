package `in`.hridayan.ashell.settings.data.backup


import `in`.hridayan.ashell.core.common.SettingsKeys

import `in`.hridayan.ashell.core.common.domain.backup.BackupProvider
import `in`.hridayan.ashell.core.domain.repository.SettingsRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import javax.inject.Inject

class SettingsBackupProvider @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val json: Json
) : BackupProvider {

    override val featureId: String = "settings"

    override suspend fun getBackupData(): JsonElement? {
        val prefs = settingsRepository.getCurrentSettings()
        val settingsMap = prefs.filterKeys { it !in settingsRepository.getPreserveKeys() }
            .mapValues { it.value?.toString() }
        if (settingsMap.isEmpty()) return null
        return json.encodeToJsonElement(settingsMap)
    }

    override suspend fun restoreData(data: JsonElement?, legacyData: (String) -> JsonElement?) {
        val jsonData = data ?: legacyData("settings") ?: return

        try {
            val settingsMap = json.decodeFromJsonElement<Map<String, String?>>(jsonData)

            settingsRepository.resetAndRestoreDefaults()

            settingsMap.forEach { (key, value) ->
                if (key in settingsRepository.getPreserveKeys()) return@forEach

                val settingKey = SettingsKeys.entries.find { it.name == key } ?: return@forEach

                value?.let {
                    @Suppress("UNCHECKED_CAST")
                    when (settingKey.default) {
                        is Boolean -> settingsRepository.setBoolean(
                            settingKey as SettingsKeys<Boolean>,
                            it.toBooleanStrictOrNull() ?: return@forEach
                        )

                        is Int -> settingsRepository.setInt(
                            settingKey as SettingsKeys<Int>,
                            it.toIntOrNull() ?: return@forEach
                        )

                        is Float -> settingsRepository.setFloat(
                            settingKey as SettingsKeys<Float>,
                            it.toFloatOrNull() ?: return@forEach
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
