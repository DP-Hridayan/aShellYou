package `in`.hridayan.ashell.logcat.presentation.model

/** Which logs of the active tab an export covers. */
enum class ExportScope {
    /** The rows picked in selection mode. */
    SELECTION,

    /** Every row the tab shows after filters and search. */
    ALL,
}
