package `in`.hridayan.ashell.settings.presentation.provider

import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.domain.model.TerminalFontStyle
import `in`.hridayan.ashell.settings.presentation.model.ButtonGroupOptions

class ButtonGroupOptionsProvider {
    companion object {
        val terminalFontOptions = listOf(
            ButtonGroupOptions(
                value = TerminalFontStyle.MONOSPACE,
                labelResId = R.string.monospace
            ),
            ButtonGroupOptions(
                value = TerminalFontStyle.SYSTEM_FONT,
                labelResId = R.string.system_font
            )
        )
    }
}