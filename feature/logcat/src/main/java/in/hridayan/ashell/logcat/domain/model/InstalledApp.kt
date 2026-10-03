package `in`.hridayan.ashell.logcat.domain.model

/** An app installed on this device, as offered by the package picker. */
data class InstalledApp(
    val packageName: String,

    /** The app's display name, as shown under its launcher icon. */
    val label: String,
)

/** Whether [query] appears in the app's name or package, ignoring case. A blank query matches. */
fun InstalledApp.matches(query: String): Boolean =
    query.isBlank() ||
        label.contains(query, ignoreCase = true) ||
        packageName.contains(query, ignoreCase = true)
