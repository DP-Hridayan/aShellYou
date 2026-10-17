package `in`.hridayan.ashell.portforward.domain.repository

/**
 * Answers whether Android's local network permission stands between the app and a host.
 *
 * Without the permission a blocked connection is dropped silently and only times out, so callers
 * check first instead of waiting for the timeout.
 */
interface LocalNetworkGate {

    /** True when [host] is on the local network and the app may not reach it. */
    suspend fun isBlocked(host: String): Boolean

    /** True when [host] resolves to the local network, whether or not access is granted. */
    suspend fun isLocal(host: String): Boolean

    fun isGranted(): Boolean
}
