package `in`.hridayan.ashell.logcat.domain.model

/**
 * Where a restarted logcat stream picks up, so that stopping and starting a stream does not
 * read the device's backlog again.
 *
 * logcat's `-T` option starts from a timestamp and includes entries at that exact timestamp, so a
 * restarted stream re-sends some entries that are already held. [heldAtTimestamp] identifies
 * those, so each can be dropped once.
 */
data class ResumePoint(
    /** Timestamp of the newest held entry, in logcat's threadtime form `MM-DD HH:MM:SS.mmm`. */
    val timestamp: String,

    /** Every held entry stamped with [timestamp], in arrival order. */
    val heldAtTimestamp: List<LogEntry>,
)
