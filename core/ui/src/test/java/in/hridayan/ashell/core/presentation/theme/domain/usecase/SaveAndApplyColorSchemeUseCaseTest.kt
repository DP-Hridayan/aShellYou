package `in`.hridayan.ashell.core.presentation.theme.domain.usecase

import androidx.datastore.preferences.core.Preferences
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.theme.data.CustomColorSchemeDao
import `in`.hridayan.ashell.core.presentation.theme.data.UserGeneratedColorSchemeEntity
import `in`.hridayan.ashell.core.presentation.theme.domain.model.UserGeneratedColorScheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveAndApplyColorSchemeUseCaseTest {

    private class FakeCustomColorSchemeDao : CustomColorSchemeDao {
        var insertedEntity: UserGeneratedColorSchemeEntity? = null
        var nextId: Long = 42L

        override fun getAllColorSchemes(): Flow<List<UserGeneratedColorSchemeEntity>> = emptyFlow()
        override suspend fun getAllColorSchemesOnce(): List<UserGeneratedColorSchemeEntity> = emptyList()
        override suspend fun getColorSchemeById(id: Int): UserGeneratedColorSchemeEntity? = null
        override fun getColorSchemeByIdFlow(id: Int): Flow<UserGeneratedColorSchemeEntity?> = emptyFlow()

        override suspend fun insertColorScheme(scheme: UserGeneratedColorSchemeEntity): Long {
            insertedEntity = scheme
            return nextId
        }

        override suspend fun updateColorScheme(scheme: UserGeneratedColorSchemeEntity) {}
        override suspend fun deleteAllColorSchemes() {}
        override suspend fun deleteColorScheme(scheme: UserGeneratedColorSchemeEntity) {}
    }

    private class FakeSettingsRepository : SettingsRepository {
        val booleanSettings = mutableMapOf<SettingsKeys<Boolean>, Boolean>()
        val intSettings = mutableMapOf<SettingsKeys<Int>, Int>()

        override val preferences: Flow<Preferences> = emptyFlow()

        override fun getBoolean(key: SettingsKeys<Boolean>): Flow<Boolean> = emptyFlow()
        override suspend fun setBoolean(key: SettingsKeys<Boolean>, value: Boolean) {
            booleanSettings[key] = value
        }
        override suspend fun toggleSetting(key: SettingsKeys<Boolean>) {}

        override fun getInt(key: SettingsKeys<Int>): Flow<Int> = emptyFlow()
        override suspend fun setInt(key: SettingsKeys<Int>, value: Int) {
            intSettings[key] = value
        }

        override fun getFloat(key: SettingsKeys<Float>): Flow<Float> = emptyFlow()
        override suspend fun setFloat(key: SettingsKeys<Float>, value: Float) {}

        override fun getString(key: SettingsKeys<String>): Flow<String> = emptyFlow()
        override suspend fun setString(key: SettingsKeys<String>, value: String) {}

        override fun getAllDefaultSettings(): Map<String, Any?> = emptyMap()
        override fun getPreserveKeys(): Set<String> = emptySet()
        override suspend fun getCurrentSettings(): Map<String, Any?> = emptyMap()
        override suspend fun resetAndRestoreDefaults(): Boolean = true
    }

    @Test
    fun saveAndApplyColorScheme_insertsSchemeAndUpdatesSettings() = runBlocking {
        val fakeDao = FakeCustomColorSchemeDao()
        val fakeSettings = FakeSettingsRepository()
        val useCase = SaveAndApplyColorSchemeUseCase(fakeDao, fakeSettings)

        val sampleScheme = UserGeneratedColorScheme(
            id = 0,
            name = "Test Scheme",
            primary = "#112233",
            onPrimary = "#FFFFFF",
            primaryContainer = "#334455",
            onPrimaryContainer = "#FFFFFF",
            inversePrimary = "#556677",
            secondary = "#223344",
            onSecondary = "#FFFFFF",
            secondaryContainer = "#445566",
            onSecondaryContainer = "#FFFFFF",
            tertiary = "#334455",
            onTertiary = "#FFFFFF",
            tertiaryContainer = "#556677",
            onTertiaryContainer = "#FFFFFF",
            error = "#FF0000",
            onError = "#FFFFFF",
            errorContainer = "#880000",
            onErrorContainer = "#FFFFFF",
            background = "#000000",
            onBackground = "#FFFFFF",
            surface = "#111111",
            onSurface = "#FFFFFF",
            surfaceVariant = "#222222",
            onSurfaceVariant = "#EEEEEE",
            surfaceTint = "#112233",
            inverseSurface = "#EEEEEE",
            inverseOnSurface = "#111111",
            surfaceBright = "#333333",
            surfaceDim = "#050505",
            surfaceContainer = "#1A1A1A",
            surfaceContainerHigh = "#2A2A2A",
            surfaceContainerHighest = "#3A3A3A",
            surfaceContainerLow = "#121212",
            surfaceContainerLowest = "#0A0A0A",
            outline = "#777777",
            outlineVariant = "#444444",
            scrim = "#000000",
            isDarkTheme = true
        )

        val result = useCase(sampleScheme)

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull())
        assertEquals("Test Scheme", fakeDao.insertedEntity?.name)
        assertEquals(true, fakeSettings.booleanSettings[SettingsKeys.UserGeneratedColorSchemeApplied])
        assertEquals(42, fakeSettings.intSettings[SettingsKeys.AppliedCustomThemeId])
        assertEquals(false, fakeSettings.booleanSettings[SettingsKeys.DynamicColors])
        assertEquals(true, fakeSettings.booleanSettings[SettingsKeys.IsCustomColorSchemeDarkThemed])
    }
}
