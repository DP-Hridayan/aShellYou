package `in`.hridayan.ashell.core.ui.provider

import `in`.hridayan.ashell.core.resources.R

import androidx.appcompat.app.AppCompatDelegate
import `in`.hridayan.ashell.core.common.domain.model.GithubReleaseType
import `in`.hridayan.ashell.core.domain.model.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.core.domain.model.AppFont
import `in`.hridayan.ashell.core.common.domain.model.BackupFrequency
import `in`.hridayan.settingsdsl.model.RadioButtonOption
import `in`.hridayan.ashell.core.common.domain.model.BackupType

class RadioGroupOptionsProvider {
    companion object {
        val darkModeOptions: List<RadioButtonOption> = listOf(
            RadioButtonOption(
                value = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                labelResId = R.string.system
            ),
            RadioButtonOption(
                value = AppCompatDelegate.MODE_NIGHT_NO,
                labelResId = R.string.off
            ),
            RadioButtonOption(
                value = AppCompatDelegate.MODE_NIGHT_YES,
                labelResId = R.string.on
            )
        )

        val updateChannelOptions: List<RadioButtonOption> = listOf(
            RadioButtonOption(
                value = GithubReleaseType.STABLE_FDROID,
                labelResId = R.string.stable_fdroid
            ),
            RadioButtonOption(
                value = GithubReleaseType.STABLE_GITHUB,
                labelResId = R.string.stable_github
            ),
            RadioButtonOption(
                value = GithubReleaseType.PRE_RELEASE_GITHUB,
                labelResId = R.string.pre_release_github
            ),
        )

        val localAdbShellModeOptions: List<RadioButtonOption> = listOf(
            RadioButtonOption(
                value = LocalAdbWorkingMode.BASIC,
                labelResId = R.string.basic_shell
            ),
            RadioButtonOption(
                value = LocalAdbWorkingMode.SHIZUKU,
                labelResId = R.string.shizuku
            ),
            RadioButtonOption(
                value = LocalAdbWorkingMode.ROOT,
                labelResId = R.string.root
            )
        )

        val bookmarkSortOptions: List<RadioButtonOption> = listOf(
            RadioButtonOption(
                value = SortType.AZ,
                labelResId = R.string.A_Z
            ), RadioButtonOption(
                value = SortType.ZA,
                labelResId = R.string.Z_A
            ),
            RadioButtonOption(
                value = SortType.NEWEST,
                labelResId = R.string.newest
            ),
            RadioButtonOption(
                value = SortType.OLDEST,
                labelResId = R.string.oldest
            )
        )

        val commandSortOptions: List<RadioButtonOption> = listOf(
            RadioButtonOption(
                value = SortType.AZ,
                labelResId = R.string.A_Z
            ), RadioButtonOption(
                value = SortType.ZA,
                labelResId = R.string.Z_A
            ),
            RadioButtonOption(
                value = SortType.MOST_USED,
                labelResId = R.string.most_used
            ),
            RadioButtonOption(
                value = SortType.LEAST_USED,
                labelResId = R.string.least_used
            )
        )

        val fontStyleOptions: List<RadioButtonOption> = AppFont.entries.map {
            RadioButtonOption(
                value = it.id,
                labelResId = it.labelResId
            )
        }

        val backupFrequencyOptions: List<RadioButtonOption> = listOf(
            RadioButtonOption(
                value = BackupFrequency.DAILY,
                labelResId = R.string.daily
            ),
            RadioButtonOption(
                value = BackupFrequency.WEEKLY,
                labelResId = R.string.weekly
            ),
            RadioButtonOption(
                value = BackupFrequency.MONTHLY,
                labelResId = R.string.monthly
            ),
        )

        val autoBackupTypeOptions: List<RadioButtonOption> = listOf(
            RadioButtonOption(
                value = BackupType.SETTINGS_AND_DATABASE.ordinal,
                labelResId = R.string.all_data
            ),
            RadioButtonOption(
                value = BackupType.SETTINGS_ONLY.ordinal,
                labelResId = R.string.settings_only
            ),
            RadioButtonOption(
                value = BackupType.DATABASE_ONLY.ordinal,
                labelResId = R.string.databases_only
            ),
        )
    }
}
