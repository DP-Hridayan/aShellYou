package `in`.hridayan.ashell.shell.common.data.parser

import `in`.hridayan.ashell.shell.common.domain.model.PackageInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemotePackageParserTest {

    private val parser = RemotePackageParser()

    @Test
    fun parseDelimitedSystemAndUserPackages() {
        val lines = listOf(
            "package:com.android.settings",
            "package:com.google.android.gms",
            parser.delimiter,
            "package:org.telegram.messenger",
            "package:com.spotify.music"
        )

        val result = parser.parse(lines)

        val expected = listOf(
            PackageInfo(packageName = "com.android.settings", isSystemApp = true),
            PackageInfo(packageName = "com.google.android.gms", isSystemApp = true),
            PackageInfo(packageName = "com.spotify.music", isSystemApp = false),
            PackageInfo(packageName = "org.telegram.messenger", isSystemApp = false)
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseHandlesCarriageReturnsAndUidSuffix() {
        val lines = listOf(
            "package:com.android.chrome uid:10077\r",
            parser.delimiter + "\r",
            "package:org.mozilla.firefox  uid:10123\r"
        )

        val result = parser.parse(lines)

        val expected = listOf(
            PackageInfo(packageName = "com.android.chrome", isSystemApp = true),
            PackageInfo(packageName = "org.mozilla.firefox", isSystemApp = false)
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseIgnoresNonPackageLinesAndEmptyLines() {
        val lines = listOf(
            "",
            "WARNING: linker: some warning",
            "package:com.android.systemui",
            "   ",
            parser.delimiter,
            "another arbitrary output",
            "package:com.example.app"
        )

        val result = parser.parse(lines)

        val expected = listOf(
            PackageInfo(packageName = "com.android.systemui", isSystemApp = true),
            PackageInfo(packageName = "com.example.app", isSystemApp = false)
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseFallbackWithoutDelimiterDefaultsToUserApp() {
        val lines = listOf(
            "package:com.example.one",
            "package:com.example.two"
        )

        val result = parser.parse(lines)

        val expected = listOf(
            PackageInfo(packageName = "com.example.one", isSystemApp = false),
            PackageInfo(packageName = "com.example.two", isSystemApp = false)
        )
        assertEquals(expected, result)
    }

    @Test
    fun parseEmptyLinesReturnsEmptyList() {
        val result = parser.parse(emptyList())
        assertTrue(result.isEmpty())
    }
}
