package `in`.hridayan.ashell.settings.presentation.provider

import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.qstiles.domain.model.TileExecutionMode
import `in`.hridayan.ashell.settings.presentation.model.ButtonGroupOptions

/**
 * Provides [ButtonGroupOptions] lists for non-settings button groups
 * (e.g. the QS Tile creation screen).
 *
 * Settings-DSL screens should use [in.hridayan.settingsdsl.model.ButtonGroupOption] instead.
 */
class ButtonGroupOptionsProvider {
    companion object {
        val tileServiceAdbExecutionMethod = listOf(
            ButtonGroupOptions(
                value = TileExecutionMode.SHIZUKU,
                labelResId = R.string.shizuku
            ),
            ButtonGroupOptions(
                value = TileExecutionMode.ROOT,
                labelResId = R.string.root
            ),
        )
    }
}
