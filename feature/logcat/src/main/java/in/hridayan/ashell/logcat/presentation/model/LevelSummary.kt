package `in`.hridayan.ashell.logcat.presentation.model

import `in`.hridayan.ashell.logcat.domain.model.FilterMode
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.model.LogLevel

/**
 * Levels a profile can select, in severity order. Silent and unknown levels are not offered
 * because logcat does not print entries at those levels.
 */
val SelectableLogLevels: List<LogLevel> = listOf(
    LogLevel.VERBOSE,
    LogLevel.DEBUG,
    LogLevel.INFO,
    LogLevel.WARNING,
    LogLevel.ERROR,
    LogLevel.FATAL,
)

/** How a profile's level selection reads in the profile list. */
sealed interface LevelSummary {
    data object AllLevels : LevelSummary

    /** An Include profile showing only [levels], in severity order. */
    data class Only(val levels: List<LogLevel>) : LevelSummary

    /** An Exclude profile hiding [levels], in severity order. */
    data class AllExcept(val levels: List<LogLevel>) : LevelSummary
}

fun LogFilter.levelSummary(): LevelSummary {
    val selected = SelectableLogLevels.filter { it in levels }
    val selectsAll = selected.size == SelectableLogLevels.size
    return when (mode) {
        FilterMode.INCLUDE ->
            if (selected.isEmpty() || selectsAll) LevelSummary.AllLevels else LevelSummary.Only(selected)

        FilterMode.EXCLUDE ->
            if (selected.isEmpty()) LevelSummary.AllLevels else LevelSummary.AllExcept(selected)
    }
}
