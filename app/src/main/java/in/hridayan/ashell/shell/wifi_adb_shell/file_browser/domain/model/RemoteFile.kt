package `in`.hridayan.ashell.shell.wifi_adb_shell.file_browser.domain.model

/**
 * Represents a file or directory on the remote device.
 */
data class RemoteFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val permissions: String = "",
    val lastModified: String = "",
    val owner: String = "",
    val group: String = ""
) {
    val isParentDirectory: Boolean get() = name == ".."
    
    val extension: String
        get() = if (!isDirectory && name.contains(".")) {
            name.substringAfterLast(".")
        } else ""
    
    val displaySize: String
        get() = when {
            isDirectory -> ""
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0))
        }
}
