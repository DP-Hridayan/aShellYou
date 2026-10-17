package `in`.hridayan.ashell.portforward.data.engine

import java.util.concurrent.Semaphore

/** Limits how many connections one tunnel relays at the same time. */
class StreamCap(limit: Int) {

    private val permits = Semaphore(limit)

    fun tryAcquire(): Boolean = permits.tryAcquire()

    fun release() = permits.release()
}
