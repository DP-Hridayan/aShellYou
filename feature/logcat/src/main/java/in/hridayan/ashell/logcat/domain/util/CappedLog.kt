package `in`.hridayan.ashell.logcat.domain.util

/**
 * A list of log entries paired with the measured size of its contents, so that a byte
 * budget can be enforced without re-measuring every retained entry on each append.
 */
internal data class CappedLog<T>(
    val items: List<T> = emptyList(),
    val bytes: Long = 0L,
)

/**
 * Builds a [CappedLog] from [items], keeping the newest entries that fit in [maxBytes].
 */
internal fun <T> cappedLogOf(
    items: List<T>,
    maxBytes: Long,
    sizeOf: (T) -> Int,
): CappedLog<T> = CappedLog(items, items.sumOf { sizeOf(it).toLong() }).trimmedTo(maxBytes, sizeOf)

/**
 * Appends [batch] and evicts the oldest entries until the total fits in [maxBytes].
 * The newest entry is always retained, even when it alone exceeds the budget.
 */
internal fun <T> CappedLog<T>.append(
    batch: List<T>,
    maxBytes: Long,
    sizeOf: (T) -> Int,
): CappedLog<T> {
    if (batch.isEmpty()) return trimmedTo(maxBytes, sizeOf)
    val grown = CappedLog(items + batch, bytes + batch.sumOf { sizeOf(it).toLong() })
    return grown.trimmedTo(maxBytes, sizeOf)
}

internal fun <T> CappedLog<T>.trimmedTo(maxBytes: Long, sizeOf: (T) -> Int): CappedLog<T> {
    if (bytes <= maxBytes) return this
    var dropped = 0
    var remaining = bytes
    while (dropped < items.lastIndex && remaining > maxBytes) {
        remaining -= sizeOf(items[dropped])
        dropped++
    }
    return CappedLog(items.subList(dropped, items.size).toList(), remaining)
}
