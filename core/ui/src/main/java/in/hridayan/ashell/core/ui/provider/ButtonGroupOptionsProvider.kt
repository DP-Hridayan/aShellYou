package `in`.hridayan.ashell.core.ui.provider

import `in`.hridayan.ashell.core.common.R

import `in`.hridayan.ashell.core.common.domain.model.TileExecutionMode
import `in`.hridayan.ashell.core.ui.components.ButtonGroupOptions

/**
 * Provides [ButtonGroupOptions] lists for non-settings button groups
 * (e.g. the QS Tile creation screen).
 *
 * Settings-DSL screens should use [in.hridayan.settingsdsl.model.ButtonGroupOption] instead.
 */
class ButtonGroupOptionsProvider {
    companion object {
        val tileServiceAdbExecutionMethod = listOf<ButtonGroupOptions<Int>>(
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
