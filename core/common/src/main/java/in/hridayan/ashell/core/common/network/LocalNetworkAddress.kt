package `in`.hridayan.ashell.core.common.network

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

/**
 * Classifies addresses the way Android's Local Network Protection does.
 *
 * Android 17 blocks traffic to these ranges for apps that target it and do not hold the local
 * network permission. Loopback is never blocked, so it is not treated as local here.
 */
object LocalNetworkAddress {

    private const val MDNS_SUFFIX = ".local"
    private const val OCTET_MASK = 0xFF
    private const val IPV6_HIGH_BYTE_MASK = 0xFE
    private const val IPV6_UNIQUE_LOCAL_PREFIX = 0xFC

    private val IPV4_PRIVATE_RANGES = listOf(
        Ipv4Range(base = 10, second = null, prefixBits = 8),
        Ipv4Range(base = 172, second = 16, prefixBits = 12),
        Ipv4Range(base = 192, second = 168, prefixBits = 16),
        Ipv4Range(base = 169, second = 254, prefixBits = 16),
        Ipv4Range(base = 100, second = 64, prefixBits = 10)
    )

    /** True when [host] is an mDNS name, which always resolves on the local network. */
    fun isMdnsName(host: String): Boolean = host.trimEnd('.').endsWith(MDNS_SUFFIX, ignoreCase = true)

    /** True when [address] is in a range that Local Network Protection restricts. */
    fun isLocal(address: InetAddress): Boolean = when {
        address.isLoopbackAddress -> false
        address is Inet4Address -> isLocalIpv4(address.address)
        address is Inet6Address -> isLocalIpv6(address)
        else -> false
    }

    private fun isLocalIpv4(bytes: ByteArray): Boolean {
        val value = bytes.fold(0L) { acc, byte -> (acc shl Byte.SIZE_BITS) or (byte.toInt() and OCTET_MASK).toLong() }
        return IPV4_PRIVATE_RANGES.any { it.contains(value) }
    }

    private fun isLocalIpv6(address: Inet6Address): Boolean {
        if (address.isLinkLocalAddress || address.isSiteLocalAddress) return true
        val first = address.address.first().toInt() and OCTET_MASK
        return first and IPV6_HIGH_BYTE_MASK == IPV6_UNIQUE_LOCAL_PREFIX
    }

    private class Ipv4Range(base: Int, second: Int?, private val prefixBits: Int) {
        private val network: Long =
            (base.toLong() shl FIRST_OCTET_SHIFT) or ((second ?: 0).toLong() shl SECOND_OCTET_SHIFT)
        private val mask: Long = (IPV4_ALL_BITS shl (IPV4_BITS - prefixBits)) and IPV4_ALL_BITS

        fun contains(value: Long): Boolean = value and mask == network and mask

        private companion object {
            const val IPV4_BITS = 32
            const val IPV4_ALL_BITS = 0xFFFFFFFFL
            const val FIRST_OCTET_SHIFT = 24
            const val SECOND_OCTET_SHIFT = 16
        }
    }
}
