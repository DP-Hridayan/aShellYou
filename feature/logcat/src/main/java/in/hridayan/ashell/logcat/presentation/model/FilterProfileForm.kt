package `in`.hridayan.ashell.logcat.presentation.model

import androidx.compose.runtime.Immutable
import `in`.hridayan.ashell.logcat.domain.model.DefaultIncludeLevels
import `in`.hridayan.ashell.logcat.domain.model.FilterMode
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.model.LogLevel

private const val VALUE_SEPARATOR = ','

/**
 * A filter profile as the editor holds it while the user types.
 *
 * [tags], [pids], [tids] and [packages] keep the raw text, including trailing commas and spaces,
 * and are only split into values by [toProfile]. Parsing on every keystroke would delete a comma
 * the moment it is typed, making a second value impossible to enter.
 */
@Immutable
data class FilterProfileForm(
    val name: String = "",
    val mode: FilterMode = FilterMode.INCLUDE,
    val levels: Set<LogLevel> = DefaultIncludeLevels,
    val tags: String = "",
    val pids: String = "",
    val tids: String = "",
    val packages: String = "",

    /** True once a save was attempted with a blank name, so the error is not shown up front. */
    val showNameError: Boolean = false,
) {
    val isValid: Boolean get() = name.isNotBlank()

    val selectedPackages: Set<String> get() = valuesOf(packages)

    fun withMode(newMode: FilterMode): FilterProfileForm =
        if (newMode == mode) this else copy(mode = newMode, levels = defaultLevelsFor(newMode))

    fun withLevelToggled(level: LogLevel): FilterProfileForm =
        copy(levels = if (level in levels) levels - level else levels + level)

    /** Adds or removes one package, keeping the others in the order they were entered. */
    fun withPackageToggled(packageName: String): FilterProfileForm {
        val current = selectedPackages
        val updated = if (packageName in current) current - packageName else current + packageName
        return copy(packages = textOf(updated))
    }

    fun toProfile(id: String): LogFilter = LogFilter(
        id = id,
        name = name.trim(),
        levels = levels,
        pids = valuesOf(pids),
        tids = valuesOf(tids),
        tags = valuesOf(tags),
        packages = valuesOf(packages),
        mode = mode,
    )

    companion object {
        fun from(profile: LogFilter): FilterProfileForm = FilterProfileForm(
            name = profile.name,
            mode = profile.mode,
            levels = profile.levels,
            tags = textOf(profile.tags),
            pids = textOf(profile.pids),
            tids = textOf(profile.tids),
            packages = textOf(profile.packages),
        )
    }
}

private fun defaultLevelsFor(mode: FilterMode): Set<LogLevel> = when (mode) {
    FilterMode.INCLUDE -> DefaultIncludeLevels
    FilterMode.EXCLUDE -> emptySet()
}

private fun valuesOf(text: String): Set<String> =
    text.split(VALUE_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }.toSet()

private fun textOf(values: Set<String>): String = values.joinToString("$VALUE_SEPARATOR ")
