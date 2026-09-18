package `in`.hridayan.ashell.logcat.domain.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

private const val BACKLOG_BATCH_SIZE = 200
private const val BACKLOG_WINDOW_MULTIPLIER = 5

/**
 * Groups upstream elements into lists. A batch opens when the first element
 * arrives and closes a window later, so at most one batch is emitted per
 * window while the upstream is busy. No element is ever dropped.
 *
 * The window widens while a backlog is draining, so a flood such as the initial
 * logcat buffer dump produces fewer, larger batches instead of saturating the
 * consumer with one update per [windowMs].
 */
fun <T> Flow<T>.batchByTime(windowMs: Long): Flow<List<T>> = channelFlow {
    val pending = Channel<T>(Channel.UNLIMITED)
    launch {
        collect { pending.send(it) }
        pending.close()
    }
    var window = windowMs
    while (true) {
        val first = pending.receiveCatching().getOrNull() ?: break
        delay(window)
        val batch = drainPending(first, pending)
        window = nextBatchWindowMs(batch.size, windowMs)
        send(batch)
    }
}

/**
 * Window to wait before closing the next batch, given how large the last one was.
 */
internal fun nextBatchWindowMs(lastBatchSize: Int, baseWindowMs: Long): Long =
    if (lastBatchSize >= BACKLOG_BATCH_SIZE) {
        baseWindowMs * BACKLOG_WINDOW_MULTIPLIER
    } else {
        baseWindowMs
    }

private fun <T> drainPending(first: T, pending: ReceiveChannel<T>): List<T> = buildList {
    add(first)
    while (true) {
        add(pending.tryReceive().getOrNull() ?: break)
    }
}
