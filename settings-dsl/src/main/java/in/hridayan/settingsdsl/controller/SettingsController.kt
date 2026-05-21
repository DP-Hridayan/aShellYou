package `in`.hridayan.settingsdsl.controller

import `in`.hridayan.settingsdsl.model.SettingsKey

/**
 * Provides state and handles events for settings UI rendering.
 *
 * Implement this once in your app (typically backed by a ViewModel/DataStore),
 * then pass it to `settingsContent()` instead of individual lambdas.
 *
 * All state-reading functions ([isChecked], [selectedValue]) are called inside
 * `@Composable` item blocks in a `LazyColumn`, so implementations may safely
 * use Compose state APIs internally.
 *
 * Example:
 * ```kotlin
 * class MySettingsController(
 *     private val viewModel: SettingsViewModel,
 * ) : SettingsController {
 *     override fun isChecked(key: SettingsKey<*>) = viewModel.isChecked(key)
 *     override fun selectedValue(key: SettingsKey<*>) = viewModel.selectedValue(key)
 *     override fun onItemClick(key: SettingsKey<*>) = viewModel.onItemClicked(key)
 *     override fun onBooleanToggle(key: SettingsKey<*>) = viewModel.onToggle(key)
 *     override fun onIntChanged(key: SettingsKey<*>, value: Int) = viewModel.setInt(key, value)
 * }
 * ```
 */
interface SettingsController {
    /** Current Boolean value for a switch/toggle item. */
    fun isChecked(key: SettingsKey<*>): Boolean

    /** Current Int value for radio/button group items. */
    fun selectedValue(key: SettingsKey<*>): Int

    /** Called when a clickable item is tapped. */
    fun onItemClick(key: SettingsKey<*>)

    /** Called when a switch/toggle is flipped. */
    fun onBooleanToggle(key: SettingsKey<*>)

    /** Called when a radio/button group selection changes. */
    fun onIntChanged(key: SettingsKey<*>, value: Int)
}
