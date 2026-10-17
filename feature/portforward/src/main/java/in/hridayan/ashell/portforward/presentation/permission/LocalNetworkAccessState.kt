package `in`.hridayan.ashell.portforward.presentation.permission

/**
 * The state of Android's local network permission, re-read whenever the screen resumes so that a
 * change made in system settings shows up at once.
 *
 * @property permanentlyDenied the system will no longer show its prompt; only settings can grant it.
 */
data class LocalNetworkAccessState(
    val granted: Boolean,
    val permanentlyDenied: Boolean,
    val request: () -> Unit,
    val openSettings: () -> Unit
)
