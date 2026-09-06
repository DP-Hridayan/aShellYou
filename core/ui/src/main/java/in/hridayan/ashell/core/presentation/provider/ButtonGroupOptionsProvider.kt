package `in`.hridayan.ashell.core.presentation.provider

import `in`.hridayan.ashell.core.common.domain.model.TileExecutionMode
import `in`.hridayan.ashell.core.presentation.components.buttongroup.ButtonGroupOptions
import `in`.hridayan.ashell.core.resources.R

/**
 * Provides [ButtonGroupOptions] lists for non-settings button groups
 * (e.g. the QS Tile creation screen).
 *
 * Settings-Graph screens should use [in.hridayan.settingsgraph.model.ButtonGroupOption] instead.
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
