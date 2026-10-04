package `in`.hridayan.ashell.shell.file_browser.domain.util

/** String helpers for absolute paths on the remote device, which always use `/`. */
object RemotePaths {

    private const val SEPARATOR = "/"

    fun childPath(directory: String, name: String): String {
        val parent = directory.trimEnd('/')
        return "$parent$SEPARATOR$name"
    }

    fun fileName(path: String): String = path.trimEnd('/').substringAfterLast(SEPARATOR)

    /** True when [path] is [ancestor] or lies anywhere below it. */
    fun isSameOrInside(path: String, ancestor: String): Boolean {
        val normalizedAncestor = ancestor.trimEnd('/')
        val normalizedPath = path.trimEnd('/')
        return normalizedPath == normalizedAncestor ||
                normalizedPath.startsWith(normalizedAncestor + SEPARATOR)
    }
}
