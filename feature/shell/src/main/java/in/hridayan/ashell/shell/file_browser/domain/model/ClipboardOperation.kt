package `in`.hridayan.ashell.shell.file_browser.domain.model

/**
 * Represents clipboard operations in the file browser.
 */
enum class ClipboardOperation {
    COPY,
    MOVE,
    COPY_BATCH,
    MOVE_BATCH;

    val isCopy: Boolean
        get() = this == COPY || this == COPY_BATCH

    val isMove: Boolean
        get() = this == MOVE || this == MOVE_BATCH

    val isBatch: Boolean
        get() = this == COPY_BATCH || this == MOVE_BATCH
}
