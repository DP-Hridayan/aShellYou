package `in`.hridayan.ashell.settings.presentation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import `in`.hridayan.settingsdsl.controller.SettingsController
import `in`.hridayan.settingsdsl.controller.rememberController

/**
 * App-specific convenience wrapper around the library's [rememberController].
 *
 * Reads one [Preferences] snapshot (single DataStore flow) and maps
 * key lookups + actions to the app's ViewModel. No pre-warming needed.
 */
@Suppress("UNCHECKED_CAST")
@Composable
fun SettingsViewModel.rememberController(): SettingsController {
    val prefs by preferences.collectAsState()
    val vm = this

    return rememberController(
        isChecked = { key ->
            val sk = key as? SettingsKeys<*> ?: return@rememberController false
            if (sk.default !is Boolean) return@rememberController false
            prefs[booleanPreferencesKey(sk.name)] ?: (sk.default as Boolean)
        },
        selectedValue = { key ->
            val sk = key as? SettingsKeys<*> ?: return@rememberController -1
            if (sk.default !is Int) return@rememberController -1
            prefs[intPreferencesKey(sk.name)] ?: (sk.default as Int)
        },
        onItemClick = { key -> vm.onItemClicked(key as SettingsKeys<*>) },
        onBooleanToggle = { key -> vm.onToggle(key as SettingsKeys<Boolean>) },
        onIntChanged = { key, v -> vm.setInt(key as SettingsKeys<Int>, v) },
    )
}
