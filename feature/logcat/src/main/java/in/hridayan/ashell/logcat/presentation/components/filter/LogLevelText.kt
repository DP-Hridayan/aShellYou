package `in`.hridayan.ashell.logcat.presentation.components.filter

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import `in`.hridayan.ashell.logcat.presentation.model.LevelSummary

private const val LEVEL_SEPARATOR = ", "

@Composable
fun logLevelName(level: LogLevel): String = stringResource(
    when (level) {
        LogLevel.VERBOSE -> R.string.verbose
        LogLevel.DEBUG -> R.string.debug
        LogLevel.INFO -> R.string.info
        LogLevel.WARNING -> R.string.warning
        LogLevel.ERROR -> R.string.error
        LogLevel.FATAL -> R.string.fatal
        LogLevel.SILENT, LogLevel.UNKNOWN -> R.string.unknown
    }
)

/** The description shown under a profile's name in the profile list. */
@Composable
fun levelSummaryText(summary: LevelSummary): String = when (summary) {
    LevelSummary.AllLevels -> stringResource(R.string.all_log_levels)
    is LevelSummary.Only -> levelNames(summary.levels)
    is LevelSummary.AllExcept -> stringResource(R.string.all_log_levels_except, levelNames(summary.levels))
}

@Composable
private fun levelNames(levels: List<LogLevel>): String =
    levels.map { logLevelName(it) }.joinToString(LEVEL_SEPARATOR)
