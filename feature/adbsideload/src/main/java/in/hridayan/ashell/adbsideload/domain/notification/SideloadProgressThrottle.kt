package `in`.hridayan.ashell.adbsideload.domain.notification

/**
 * Decides when the ongoing notification is worth rebuilding.
 *
 * Serving a large package emits progress thousands of times. Posting all of them would be throttled
 * by the system anyway and would waste power, so an update is published when it changes something
 * the user can see and not more often than [minIntervalMs].
 */
class SideloadProgressThrottle(
    private val minIntervalMs: Long = DEFAULT_INTERVAL_MS,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private var published: SideloadNotificationUpdate? = null
    private var publishedAt = 0L

    fun shouldPublish(update: SideloadNotificationUpdate): Boolean {
        val due = isDue(update)
        if (due) {
            published = update
            publishedAt = clock()
        }
        return due
    }

    private fun isDue(update: SideloadNotificationUpdate): Boolean {
        val previous = published ?: return true
        if (previous.isIndeterminate != update.isIndeterminate) return true
        if (update.percent == previous.percent) return false
        if (update.percent >= COMPLETE_PERCENT) return true
        return clock() - publishedAt >= minIntervalMs
    }

    private companion object {
        const val DEFAULT_INTERVAL_MS = 500L
        const val COMPLETE_PERCENT = 100
    }
}
