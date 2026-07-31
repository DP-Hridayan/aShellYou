package `in`.hridayan.ashell.settings.domain.usecase


import `in`.hridayan.ashell.core.common.settings.SettingsKeys

import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository

class ToggleSettingUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(key: SettingsKeys<Boolean>) = repo.toggleSetting(key)
}
