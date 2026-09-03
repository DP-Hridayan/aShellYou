package `in`.hridayan.settingsdsl.dsl

import `in`.hridayan.settingsdsl.model.SettingsPage

/**
 * Creates a [SettingsPage] — the top-level container for a settings screen, using an idiomatic builder block.
 *
 * @param screenId Stable app-defined identifier for this screen (e.g. `"look_and_feel"`).
 * @param block Builder block for configuring the page.
 */
fun settingsPage(
    screenId: String? = null,
    block: SettingsPageBuilder.() -> Unit
): SettingsPage {
    return SettingsPageBuilder(screenId).apply(block).build()
}
