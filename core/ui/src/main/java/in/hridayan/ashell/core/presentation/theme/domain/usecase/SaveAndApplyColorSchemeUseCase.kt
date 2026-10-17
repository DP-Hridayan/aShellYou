package `in`.hridayan.ashell.core.presentation.theme.domain.usecase

import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.theme.data.CustomColorSchemeDao
import `in`.hridayan.ashell.core.presentation.theme.domain.model.UserGeneratedColorScheme
import `in`.hridayan.ashell.core.presentation.theme.domain.model.toEntity
import javax.inject.Inject

class SaveAndApplyColorSchemeUseCase @Inject constructor(
    private val customColorSchemeDao: CustomColorSchemeDao,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(scheme: UserGeneratedColorScheme): Result<Long> {
        return runCatching {
            val insertedId = customColorSchemeDao.insertColorScheme(scheme.toEntity())
            val targetId = if (scheme.id > 0) scheme.id else insertedId.toInt()

            settingsRepository.setBoolean(SettingsKeys.UserGeneratedColorSchemeApplied, true)
            settingsRepository.setInt(SettingsKeys.AppliedCustomThemeId, targetId)
            settingsRepository.setBoolean(SettingsKeys.DynamicColors, false)
            settingsRepository.setBoolean(SettingsKeys.IsCustomColorSchemeDarkThemed, scheme.isDarkTheme)

            insertedId
        }
    }
}
