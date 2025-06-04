package `in`.hridayan.ashell.settings.domain.model

data class NotificationState(
    val enableNotifications: Boolean,
    val markAttendance: Boolean,
    val missedAttendance: Boolean,
    val updateAvailable: Boolean
)
