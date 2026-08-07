package `in`.hridayan.ashell.adbsideload.domain.model

enum class SideloadStatus {
    IDLE,
    READING_FILE,
    SENDING,
    COMPLETE,
    ERROR,
    CANCELLED
}
