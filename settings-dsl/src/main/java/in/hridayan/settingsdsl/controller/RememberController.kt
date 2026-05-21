package `in`.hridayan.settingsdsl.controller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import `in`.hridayan.settingsdsl.model.SettingsKey

/**
 * Creates a [SettingsController] from individual lambda callbacks.
 *
 * This is the recommended way to build a controller in the settings-dsl library.
 * Pass your own implementations for reading state and handling events:
 *
 * ```kotlin
 * val controller = rememberController(
 *     isChecked = { key -> prefs.getBoolean(key) },
 *     selectedValue = { key -> prefs.getInt(key) },
 *     onItemClick = { key -> viewModel.onItemClicked(key) },
 *     onBooleanToggle = { key -> viewModel.onToggle(key) },
 *     onIntChanged = { key, value -> viewModel.setInt(key, value) },
 * )
 * ```
 *
 * Lambdas are kept up-to-date via [rememberUpdatedState], so they can
 * safely close over recomposition-scoped values (e.g. collected state).
 */
@Composable
fun rememberController(
    isChecked: (SettingsKey<*>) -> Boolean = { false },
    selectedValue: (SettingsKey<*>) -> Int = { -1 },
    onItemClick: (SettingsKey<*>) -> Unit = {},
    onBooleanToggle: (SettingsKey<*>) -> Unit = {},
    onIntChanged: (SettingsKey<*>, Int) -> Unit = { _, _ -> },
): SettingsController {
    val currentIsChecked by rememberUpdatedState(isChecked)
    val currentSelectedValue by rememberUpdatedState(selectedValue)
    val currentOnItemClick by rememberUpdatedState(onItemClick)
    val currentOnBooleanToggle by rememberUpdatedState(onBooleanToggle)
    val currentOnIntChanged by rememberUpdatedState(onIntChanged)

    return remember {
        object : SettingsController {
            override fun isChecked(key: SettingsKey<*>) = currentIsChecked(key)
            override fun selectedValue(key: SettingsKey<*>) = currentSelectedValue(key)
            override fun onItemClick(key: SettingsKey<*>) = currentOnItemClick(key)
            override fun onBooleanToggle(key: SettingsKey<*>) = currentOnBooleanToggle(key)
            override fun onIntChanged(key: SettingsKey<*>, value: Int) = currentOnIntChanged(key, value)
        }
    }
}
