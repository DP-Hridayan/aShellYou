package `in`.hridayan.ashell.mirror.domain.protocol

import javax.inject.Inject
import kotlin.random.Random

/**
 * Picks the session id that names the server's socket. A fresh random value per session keeps two
 * clients, such as this app and desktop scrcpy, from connecting to each other's server.
 */
class ScidGenerator(private val random: Random) {

    @Inject
    constructor() : this(Random.Default)

    /** A non-negative 31-bit value, the range the server parses. */
    fun next(): Int = random.nextInt(0, Int.MAX_VALUE)
}
