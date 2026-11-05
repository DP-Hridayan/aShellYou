package `in`.hridayan.ashell.settings.data.remote.mapper

import `in`.hridayan.ashell.core.common.domain.model.GithubReleaseType
import `in`.hridayan.ashell.settings.data.remote.dto.GithubAssetDto
import `in`.hridayan.ashell.settings.data.remote.dto.GithubReleaseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GithubReleaseMapperTest {

    @Test
    fun toDomain_mapsDownloadSizeFromMatchingApkAsset() {
        val expectedSize = 15728640L
        val dto = GithubReleaseDto(
            tagName = "v1.0.0",
            assets = listOf(
                GithubAssetDto(
                    name = "aShellYou-github-release.apk",
                    browserDownloadUrl = "https://example.com/download.apk",
                    downloadCount = 10L,
                    size = expectedSize
                )
            ),
            body = "Changelog text"
        )

        val domain = dto.toDomain(GithubReleaseType.STABLE_GITHUB)

        assertEquals("v1.0.0", domain.tagName)
        assertEquals("https://example.com/download.apk", domain.apkUrl)
        assertEquals(expectedSize, domain.downloadSize)
        assertEquals("Changelog text", domain.body)
    }

    @Test
    fun toDomain_whenNoMatchingAsset_setsDownloadSizeToNull() {
        val dto = GithubReleaseDto(
            tagName = "v1.0.0",
            assets = emptyList(),
            body = "Changelog text"
        )

        val domain = dto.toDomain(GithubReleaseType.STABLE_GITHUB)

        assertEquals("v1.0.0", domain.tagName)
        assertNull(domain.apkUrl)
        assertNull(domain.downloadSize)
        assertEquals("Changelog text", domain.body)
    }
}
