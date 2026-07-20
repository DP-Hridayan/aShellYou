package `in`.hridayan.ashell.core.common

import androidx.compose.runtime.Composable

/**
 * Interface for reading granular settings as Compose state.
 * Implemented in the app module.
 */
interface SettingsState {
    @Composable
    operator fun <T> get(key: SettingsKeys<T>): T
}
