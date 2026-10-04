package `in`.hridayan.ashell.shell.file_browser.domain.model

enum class PathKind {
    MISSING,
    FILE,
    DIRECTORY
}

/**
 * What the device reports about one path at the moment it was asked.
 *
 * A symlink to a folder reports [PathKind.DIRECTORY]; any other symlink, even a dangling one,
 * reports [PathKind.FILE].
 */
data class PathInfo(
    val kind: PathKind,

    /**
     * The path with every folder above it resolved to its physical location, so `/sdcard/a` and
     * `/storage/emulated/0/a` compare equal. The last component is kept as is, so a symlink is
     * not followed to its target. Null when the parent could not be resolved.
     */
    val realPath: String? = null
) {
    val exists: Boolean
        get() = kind != PathKind.MISSING

    val isDirectory: Boolean
        get() = kind == PathKind.DIRECTORY
}
