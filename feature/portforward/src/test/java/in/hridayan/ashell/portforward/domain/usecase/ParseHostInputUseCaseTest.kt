package `in`.hridayan.ashell.portforward.domain.usecase

import `in`.hridayan.ashell.portforward.domain.model.ParsedHostInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParseHostInputUseCaseTest {

    private val parse = ParseHostInputUseCase()

    @Test
    fun `http url with port and path`() {
        assertEquals(
            ParsedHostInput("192.168.1.10", 5173, "/admin", isHttp = true),
            parse("http://192.168.1.10:5173/admin")
        )
    }

    @Test
    fun `http url keeps the query in the path`() {
        assertEquals("/search?q=1", parse("http://nas.local:8080/search?q=1")?.path)
    }

    @Test
    fun `http url without port uses the default`() {
        assertEquals(ParsedHostInput("example.com", 80, null, isHttp = true), parse("http://example.com"))
    }

    @Test
    fun `https url without port uses the default`() {
        assertEquals(443, parse("https://example.com/")?.port)
    }

    @Test
    fun `root path is dropped`() {
        assertNull(parse("http://example.com:3000/")?.path)
    }

    @Test
    fun `host and port`() {
        assertEquals(ParsedHostInput("my-pc.local", 3000, null, isHttp = false), parse("my-pc.local:3000"))
    }

    @Test
    fun `host port and path without scheme`() {
        assertEquals(ParsedHostInput("10.0.0.2", 8000, "/docs", isHttp = false), parse("10.0.0.2:8000/docs"))
    }

    @Test
    fun `bare host has no port`() {
        assertEquals(ParsedHostInput("nas.local", null, null, isHttp = false), parse("nas.local"))
    }

    @Test
    fun `surrounding whitespace is ignored`() {
        assertEquals("nas.local", parse("  nas.local:80  ")?.host)
    }

    @Test
    fun `bracketed ipv6 with port`() {
        assertEquals(ParsedHostInput("fe80::1", 8080, null, isHttp = false), parse("[fe80::1]:8080"))
    }

    @Test
    fun `bare ipv6 without port`() {
        assertEquals(ParsedHostInput("fd00::5", null, null, isHttp = false), parse("fd00::5"))
    }

    @Test
    fun `ipv6 url`() {
        assertEquals(ParsedHostInput("::1", 9000, null, isHttp = true), parse("http://[::1]:9000"))
    }

    @Test
    fun `empty input is rejected`() {
        assertNull(parse("   "))
    }

    @Test
    fun `port out of range is rejected`() {
        assertNull(parse("host:70000"))
    }

    @Test
    fun `non numeric port is rejected`() {
        assertNull(parse("host:abc"))
    }

    @Test
    fun `host with spaces is rejected`() {
        assertNull(parse("my pc:80"))
    }

    @Test
    fun `unknown scheme without port has no port`() {
        assertEquals(ParsedHostInput("db.local", null, null, isHttp = false), parse("postgres://db.local"))
    }
}
