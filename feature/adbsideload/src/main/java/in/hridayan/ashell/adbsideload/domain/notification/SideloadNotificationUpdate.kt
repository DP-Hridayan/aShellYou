package `in`.hridayan.ashell.adbsideload.domain.notification

import `in`.hridayan.ashell.adbsideload.domain.model.SideloadOperation
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadStatus

/** What the ongoing notification's progress bar should show. */
data class SideloadNotificationUpdate(
    val percent: Int = 0,
    val isIndeterminate: Boolean = true,
)

/**
 * Phases with no measurable progress show a busy bar: recovery decides when to ask for the first
 * block, and pauses between blocks while it verifies and installs.
 */
fun SideloadOperation.toNotificationUpdate(): SideloadNotificationUpdate = when (status) {
    SideloadStatus.READING_FILE,
    SideloadStatus.WAITING_FOR_RECOVERY -> SideloadNotificationUpdate(isIndeterminate = true)

    else -> SideloadNotificationUpdate(
        percent = (progress * PERCENT).toInt().coerceIn(0, PERCENT),
        isIndeterminate = false,
    )
}

private const val PERCENT = 100
