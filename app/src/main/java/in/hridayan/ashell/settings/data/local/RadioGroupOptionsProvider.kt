package `in`.hridayan.ashell.settings.data.local

import androidx.appcompat.app.AppCompatDelegate
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.constants.GithubReleaseType
import `in`.hridayan.ashell.core.common.constants.LocalAdbWorkingMode
import `in`.hridayan.ashell.settings.data.local.model.RadioButtonOptions

class RadioGroupOptionsProvider {
    companion object {
        val darkModeOptions: List<RadioButtonOptions> = listOf(
            RadioButtonOptions(
                value = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                labelResId = R.string.system
            ),
            RadioButtonOptions(
                value = AppCompatDelegate.MODE_NIGHT_NO,
                labelResId = R.string.off
            ),
            RadioButtonOptions(
                value = AppCompatDelegate.MODE_NIGHT_YES,
                labelResId = R.string.on
            )
        )

        val updateChannelOptions: List<RadioButtonOptions> = listOf(
            RadioButtonOptions(
                value = GithubReleaseType.STABLE,
                labelResId = R.string.stable
            ),
            RadioButtonOptions(
                value = GithubReleaseType.PRE_RELEASE,
                labelResId = R.string.pre_release
            ),
        )

        val localAdbShellModeOptions: List<RadioButtonOptions> = listOf(
            RadioButtonOptions(
                value = LocalAdbWorkingMode.BASIC,
                labelResId = R.string.basic_shell
            ),
            RadioButtonOptions(
                value = LocalAdbWorkingMode.SHIZUKU,
                labelResId = R.string.shizuku
            ),
            RadioButtonOptions(
                value = LocalAdbWorkingMode.ROOT,
                labelResId = R.string.root
            )
        )
    }
}