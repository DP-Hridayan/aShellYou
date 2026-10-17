package `in`.hridayan.ashell.logcat.domain.model

/** Where exported logs are written. */
sealed interface ExportTarget {

    /** A document the user created through the system file picker. */
    data class Document(val uriString: String) : ExportTarget

    /** A private cache file named [fileName], handed to other apps through a share intent. */
    data class ShareCache(val fileName: String) : ExportTarget
}
