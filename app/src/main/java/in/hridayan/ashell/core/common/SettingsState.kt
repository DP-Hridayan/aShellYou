package `in`.hridayan.ashell.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Provides granular, per-key access to settings as Compose state.
 *
 * Each call to [get] creates an independent [State] subscription backed by
 * a [distinctUntilChanged] DataStore flow. This means only composables reading
 * a **specific changed key** recompose — toggling one switch will NOT cause
 * every settings-reading composable in the app to recompose.
 *
 * Usage:
 * ```kotlin
 * val settings = LocalSettings.current
 * val haptics = settings[SettingsKeys.HapticsAndVibration]   // Boolean
 * val theme   = settings[SettingsKeys.ThemeMode]              // Int
 * ```
 *
 * Provided via [CompositionLocals] at the app root. The instance itself is
 * stable (wraps a [SettingsViewModel]), so [LocalSettings] uses
 * `staticCompositionLocalOf` — the granularity comes from per-key
 * `collectAsState()`, not from the CompositionLocal mechanism.
 */
class SettingsState internal constructor(private val vm: SettingsViewModel) {

    /**
     * Read a single setting key as Compose state.
     *
     * The returned value is backed by a [distinctUntilChanged] flow, so this
     * composable only recomposes when **this specific key's value** changes.
     *
     * The flow is [remember]ed by [key] to avoid unnecessary coroutine restarts
     * on parent recomposition.
     *
     * @param key A typed [SettingsKeys] entry (Boolean, Int, Float, or String).
     * @return The current value of the setting, recomposing only when it changes.
     */
    @Composable
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: SettingsKeys<T>): T {
        return when (val default = key.defaultValue) {
            is Boolean -> {
                val flow = remember(key) {
                    vm.getBoolean(key as SettingsKeys<Boolean>).distinctUntilChanged()
                }
                flow.collectAsState(initial = default).value as T
            }

            is Int -> {
                val flow = remember(key) {
                    vm.getInt(key as SettingsKeys<Int>).distinctUntilChanged()
                }
                flow.collectAsState(initial = default).value as T
            }

            is Float -> {
                val flow = remember(key) {
                    vm.getFloat(key as SettingsKeys<Float>).distinctUntilChanged()
                }
                flow.collectAsState(initial = default).value as T
            }

            is String -> {
                val flow = remember(key) {
                    vm.getString(key as SettingsKeys<String>).distinctUntilChanged()
                }
                flow.collectAsState(initial = default).value as T
            }

            else -> error("Unsupported settings key type: ${key.name}")
        }
    }
}
