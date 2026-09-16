package `in`.hridayan.ashell.adbsideload.domain.model

enum class SideloadStatus {
    IDLE,
    READING_FILE,
    WAITING_FOR_RECOVERY,
    SENDING,
    COMPLETE,
    ERROR,
    CANCELLED;

    val isActive: Boolean
        get() = this == READING_FILE || this == WAITING_FOR_RECOVERY || this == SENDING

    val isFinished: Boolean
        get() = this == COMPLETE || this == ERROR || this == CANCELLED
}
