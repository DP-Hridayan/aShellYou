package `in`.hridayan.ashell.logcat.domain.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

/**
 * Groups upstream elements into lists. A batch opens when the first element
 * arrives and closes [windowMs] later, so at most one batch is emitted per
 * window while the upstream is busy. No element is ever dropped.
 */
fun <T> Flow<T>.batchByTime(windowMs: Long): Flow<List<T>> = channelFlow {
    val pending = Channel<T>(Channel.UNLIMITED)
    launch {
        collect { pending.send(it) }
        pending.close()
    }
    while (true) {
        val first = pending.receiveCatching().getOrNull() ?: break
        delay(windowMs)
        send(drainPending(first, pending))
    }
}

private fun <T> drainPending(first: T, pending: ReceiveChannel<T>): List<T> = buildList {
    add(first)
    while (true) {
        add(pending.tryReceive().getOrNull() ?: break)
    }
}
