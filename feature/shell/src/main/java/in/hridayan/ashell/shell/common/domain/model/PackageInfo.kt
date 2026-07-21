package `in`.hridayan.ashell.shell.common.domain.model

/**
 * Data class representing information about an Android package.
 *
 * @property packageName The unique identifier of the package (e.g., "com.example.app").
 * @property isSystemApp True if the package is a system application, false otherwise.
 */
data class PackageInfo(
    val packageName: String,
    val isSystemApp: Boolean
)
