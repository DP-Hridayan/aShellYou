package `in`.hridayan.ashell.core.common.domain.usecase.ai

import androidx.datastore.preferences.core.Preferences
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

internal class FakeApiKeyRepository(initialKeys: Map<String, String> = emptyMap()) :
    ApiKeyRepository {

    private val keys = MutableStateFlow(initialKeys)

    override fun setKey(provider: LlmProvider, key: String) {
        keys.value = keys.value + (provider.id to key)
    }

    override fun getKey(provider: LlmProvider): String? = keys.value[provider.id]

    override fun deleteKey(provider: LlmProvider) {
        keys.value = keys.value - provider.id
    }

    override fun hasKey(provider: LlmProvider): Flow<Boolean> =
        keys.map { it.containsKey(provider.id) }
}

internal class FakeSettingsRepository(initialStrings: Map<String, String> = emptyMap()) :
    SettingsRepository {

    private val strings = MutableStateFlow(initialStrings)

    override val preferences: Flow<Preferences> = emptyFlow()

    override fun getBoolean(key: SettingsKeys<Boolean>): Flow<Boolean> = unsupported()

    override suspend fun setBoolean(key: SettingsKeys<Boolean>, value: Boolean) = unsupported()

    override suspend fun toggleSetting(key: SettingsKeys<Boolean>) = unsupported()

    override fun getInt(key: SettingsKeys<Int>): Flow<Int> = unsupported()

    override suspend fun setInt(key: SettingsKeys<Int>, value: Int) = unsupported()

    override fun getFloat(key: SettingsKeys<Float>): Flow<Float> = unsupported()

    override suspend fun setFloat(key: SettingsKeys<Float>, value: Float) = unsupported()

    override fun getString(key: SettingsKeys<String>): Flow<String> =
        strings.map { it[key.name] ?: key.default }

    override suspend fun setString(key: SettingsKeys<String>, value: String) {
        strings.value = strings.value + (key.name to value)
    }

    override fun getAllDefaultSettings(): Map<String, Any?> = unsupported()

    override fun getPreserveKeys(): Set<String> = unsupported()

    override suspend fun getCurrentSettings(): Map<String, Any?> = unsupported()

    override suspend fun resetAndRestoreDefaults(): Boolean = unsupported()

    fun peekString(key: SettingsKeys<String>): String = strings.value[key.name] ?: key.default

    private fun unsupported(): Nothing = throw UnsupportedOperationException("Not used in tests")
}
