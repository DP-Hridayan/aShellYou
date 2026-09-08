package `in`.hridayan.ashell.logcat.domain.util

/**
 * Appends [batch] to this list while keeping at most [max] elements,
 * evicting the oldest elements first. Allocates a single result list.
 */
internal fun <T> List<T>.appendCapped(batch: List<T>, max: Int): List<T> {
    val overflow = (size + batch.size - max).coerceAtLeast(0)
    val keptOld = subList(overflow.coerceAtMost(size), size)
    val keptNew = batch.takeLast(max)
    return buildList(keptOld.size + keptNew.size) {
        addAll(keptOld)
        addAll(keptNew)
    }
}
