package `in`.hridayan.ashell.shell.file_browser.domain.model

data class PasteRequest(
    val sourcePaths: List<String>,
    val destinationDir: String,

    /** Either [OperationType.COPY] or [OperationType.MOVE]. */
    val operationType: OperationType
)

/** Progress over the items the user pasted; items inside merged folders are not counted. */
data class PasteProgress(
    /** One based position of the item being pasted. */
    val current: Int,
    val total: Int,
    val fileName: String
)

enum class PasteFailureReason {
    SOURCE_MISSING,

    /** A folder was pasted into itself or into one of its own subfolders. */
    INTO_ITSELF,

    /** Every "name (n)" candidate for keep both was already taken. */
    NO_FREE_NAME,

    /** The device rejected or did not answer a command; see [PasteFailure.message]. */
    COMMAND_FAILED
}

data class PasteFailure(
    val sourcePath: String,
    val reason: PasteFailureReason,

    /** The device's own explanation, when it gave one. */
    val message: String? = null
)

/**
 * The outcome of a paste.
 *
 * Counts cover every item touched, including items inside merged folders.
 */
data class PasteSummary(
    val completedCount: Int,
    val skippedCount: Int,
    val failures: List<PasteFailure>,

    /** True when the user stopped the paste before every item was handled. */
    val cancelled: Boolean
) {
    val failedCount: Int
        get() = failures.size
}
