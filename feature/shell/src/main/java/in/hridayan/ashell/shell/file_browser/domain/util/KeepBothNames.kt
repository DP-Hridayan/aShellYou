package `in`.hridayan.ashell.shell.file_browser.domain.util

/**
 * Generates the "name (n)" alternatives offered when the user keeps both items.
 *
 * The counter goes before the extension of a file, keeping `.tar.*` archives whole, and after the
 * full name of a folder or a dot file such as `.nomedia`. A name that already ends in a counter
 * continues from it, so `photo (1).jpg` yields `photo (2).jpg` rather than `photo (1) (1).jpg`.
 */
object KeepBothNames {

    private val counterSuffix = Regex("""^(.*) \((\d+)\)$""")
    private val compoundTarExtension = Regex("""\.tar\.[^.]+$""", RegexOption.IGNORE_CASE)

    fun candidates(name: String, isDirectory: Boolean): Sequence<String> {
        val (stem, extension) = split(name, isDirectory)
        val counterMatch = counterSuffix.find(stem)
        val base = counterMatch?.groupValues?.get(1) ?: stem
        val start = counterMatch?.groupValues?.get(2)?.toIntOrNull()?.plus(1) ?: 1
        return generateSequence(start) { it + 1 }.map { "$base ($it)$extension" }
    }

    private fun split(name: String, isDirectory: Boolean): Pair<String, String> {
        if (isDirectory) return name to ""

        val tarMatch = compoundTarExtension.find(name)
        if (tarMatch != null && tarMatch.range.first > 0) {
            return name.substring(0, tarMatch.range.first) to tarMatch.value
        }

        val dot = name.lastIndexOf('.')
        return if (dot > 0) name.substring(0, dot) to name.substring(dot) else name to ""
    }
}
