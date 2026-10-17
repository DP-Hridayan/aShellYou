package `in`.hridayan.ashell.core.common.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

class LocalNetworkAddressTest {

    private fun isLocal(literal: String) = LocalNetworkAddress.isLocal(InetAddress.getByName(literal))

    @Test
    fun `private ipv4 ranges are local`() {
        PRIVATE_ADDRESSES.forEach { assertTrue(it, isLocal(it)) }
    }

    @Test
    fun `addresses just outside the ranges are not local`() {
        OUTSIDE_ADDRESSES.forEach { assertFalse(it, isLocal(it)) }
    }

    @Test
    fun `loopback is not local`() {
        assertFalse(isLocal("127.0.0.1"))
        assertFalse(isLocal("::1"))
    }

    @Test
    fun `link local and unique local ipv6 are local`() {
        assertTrue(isLocal("fe80::1"))
        assertTrue(isLocal("fd12:3456::1"))
        assertTrue(isLocal("fc00::1"))
    }

    @Test
    fun `global ipv6 is not local`() {
        assertFalse(isLocal("2001:4860:4860::8888"))
    }

    @Test
    fun `mdns names are recognised`() {
        assertTrue(LocalNetworkAddress.isMdnsName("my-pc.local"))
        assertTrue(LocalNetworkAddress.isMdnsName("MY-PC.LOCAL."))
        assertFalse(LocalNetworkAddress.isMdnsName("local.example.com"))
    }

    private companion object {
        val PRIVATE_ADDRESSES = listOf(
            "10.0.0.1",
            "10.255.255.255",
            "172.16.0.1",
            "172.31.255.254",
            "192.168.1.10",
            "169.254.3.4",
            "100.64.0.1",
            "100.127.255.255"
        )

        val OUTSIDE_ADDRESSES = listOf(
            "11.0.0.1",
            "172.15.255.255",
            "172.32.0.1",
            "192.169.0.1",
            "169.255.0.1",
            "100.63.255.255",
            "100.128.0.1",
            "8.8.8.8"
        )
    }
}
