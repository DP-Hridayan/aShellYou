package `in`.hridayan.ashell.shizuku

import `in`.hridayan.ashell.core.common.domain.model.TileExecutionMode
import `in`.hridayan.ashell.core.common.domain.model.localadb.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuHelperPolicy
import `in`.hridayan.ashell.qstiles.domain.repository.TileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppShizukuHelperPolicy @Inject constructor(
    settingsRepository: SettingsRepository,
    tileRepository: TileRepository
) : ShizukuHelperPolicy {

    private val localAdbUsesShizuku: Flow<Boolean> =
        settingsRepository.getInt(SettingsKeys.LocalAdbWorkingMode)
            .map { mode -> mode == LocalAdbWorkingMode.SHIZUKU }

    private val tilesUseShizuku: Flow<Boolean> =
        tileRepository.getTiles()
            .map { tiles -> tiles.any { it.executionMode == TileExecutionMode.SHIZUKU } }

    override val warmUpWanted: Flow<Boolean> =
        combine(localAdbUsesShizuku, tilesUseShizuku) { shell, tiles -> shell || tiles }
            .distinctUntilChanged()

    override val keepAlive: Flow<Boolean> =
        settingsRepository.getBoolean(SettingsKeys.KeepShizukuHelperAlive)
}
