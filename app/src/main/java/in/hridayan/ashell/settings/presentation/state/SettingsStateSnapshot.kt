package `in`.hridayan.ashell.settings.presentation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.model.SettingsKey

/**
 * A snapshot of all settings values, keyed by [SettingsKeys].
 *
 * Produced by [rememberSettingsState] via a single [collectAsState] call on
 * the ViewModel's pre-warmed [SettingsViewModel.settingsState] — no per-key
 * flow subscriptions inside Compose, so no first-frame lag.
 */
class SettingsStateSnapshot internal constructor(
    private val values: Map<SettingsKeys, Any?>,
) {
    /** Current Boolean value for [key], or `false` if the key is not Boolean. */
    fun isChecked(key: SettingsKey): Boolean =
        values[key as? SettingsKeys] as? Boolean ?: false

    /** Current Int value for [key], or `-1` if the key is not Int. */
    fun selectedValue(key: SettingsKey): Int =
        values[key as? SettingsKeys] as? Int ?: -1
}

/**
 * Returns a [SettingsStateSnapshot] backed by the ViewModel's single pre-warmed
 * [StateFlow][kotlinx.coroutines.flow.StateFlow].
 *
 * This is a **single** [collectAsState] call — all settings values are already
 * in memory by the time any screen opens, because the ViewModel starts streaming
 * them from DataStore immediately on creation.
 */
@Composable
fun SettingsViewModel.rememberSettingsState(): SettingsStateSnapshot {
    val values by settingsState.collectAsState()
    return SettingsStateSnapshot(values)
}
