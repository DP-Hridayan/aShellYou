package `in`.hridayan.ashell.settings.domain.usecase

import `in`.hridayan.ashell.core.domain.model.GithubRepoStats
import `in`.hridayan.ashell.core.domain.repository.GithubDataRepository
import `in`.hridayan.ashell.settings.domain.model.UpdateResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Method

// Tests for the private isNewerVersion logic in CheckUpdateUseCase.
// Stage order: debug(0) < alpha(1) < beta(2) < stable(3)
class VersionComparisonTest {

    private lateinit var useCase: CheckUpdateUseCase
    private lateinit var isNewerVersion: Method

    // No-op stub; the repository is never touched by the pure comparison functions.
    private val stubRepository = object : GithubDataRepository {
        override suspend fun fetchLatestRelease(
            includePrerelease: Boolean,
            releaseType: Int
        ): UpdateResult = UpdateResult.UnknownError

        override fun observeRepoStats(): Flow<GithubRepoStats> = emptyFlow()

        override suspend fun refreshRepoStats() = Unit
    }

    @Before
    fun setUp() {
        useCase = CheckUpdateUseCase(repository = stubRepository)

        isNewerVersion = CheckUpdateUseCase::class.java
            .getDeclaredMethod("isNewerVersion", String::class.java, String::class.java)
            .also { it.isAccessible = true }
    }

    private fun isNewer(latest: String, current: String): Boolean =
        isNewerVersion.invoke(useCase, latest, current) as Boolean

    // Stable vs stable

    @Test
    fun `stable patch bump is newer`() {
        assertTrue(isNewer("1.0.1", "1.0.0"))
    }

    @Test
    fun `stable minor bump is newer`() {
        assertTrue(isNewer("1.1.0", "1.0.9"))
    }

    @Test
    fun `stable major bump is newer`() {
        assertTrue(isNewer("2.0.0", "1.9.9"))
    }

    @Test
    fun `same stable version is not newer`() {
        assertFalse(isNewer("1.0.0", "1.0.0"))
    }

    @Test
    fun `older stable version is not newer`() {
        assertFalse(isNewer("1.0.0", "1.0.1"))
    }

    @Test
    fun `v-prefix is stripped correctly`() {
        assertTrue(isNewer("v2.0.0", "v1.9.0"))
    }

    // Alpha progression

    @Test
    fun `alpha02 is newer than alpha01`() {
        assertTrue(isNewer("1.0.0-alpha02", "1.0.0-alpha01"))
    }

    @Test
    fun `alpha01 is not newer than alpha02`() {
        assertFalse(isNewer("1.0.0-alpha01", "1.0.0-alpha02"))
    }

    @Test
    fun `same alpha version is not newer`() {
        assertFalse(isNewer("1.0.0-alpha01", "1.0.0-alpha01"))
    }

    // Beta progression

    @Test
    fun `beta02 is newer than beta01`() {
        assertTrue(isNewer("1.0.0-beta02", "1.0.0-beta01"))
    }

    @Test
    fun `beta01 is not newer than beta02`() {
        assertFalse(isNewer("1.0.0-beta01", "1.0.0-beta02"))
    }

    // Cross-stage

    @Test
    fun `beta is newer than alpha (same version numbers)`() {
        assertTrue(isNewer("1.0.0-beta01", "1.0.0-alpha01"))
    }

    @Test
    fun `beta is newer than alpha (same version different beta alpha numbers)`() {
        assertTrue(isNewer("1.0.0-beta01", "1.0.0-alpha02"))
    }

    @Test
    fun `alpha is not newer than beta (same version numbers)`() {
        assertFalse(isNewer("1.0.0-alpha01", "1.0.0-beta01"))
    }

    @Test
    fun `stable is newer than beta (same version numbers)`() {
        assertTrue(isNewer("1.0.0", "1.0.0-beta01"))
    }

    @Test
    fun `beta is not newer than stable (same version numbers)`() {
        assertFalse(isNewer("1.0.0-beta01", "1.0.0"))
    }

    @Test
    fun `stable is newer than alpha (same version numbers)`() {
        assertTrue(isNewer("1.0.0", "1.0.0-alpha01"))
    }

    @Test
    fun `alpha is not newer than stable (same version numbers)`() {
        assertFalse(isNewer("1.0.0-alpha01", "1.0.0"))
    }

    // Version numbers take priority over stage

    @Test
    fun `alpha of newer version beats stable of older version`() {
        assertTrue(isNewer("2.0.0-alpha01", "1.9.9"))
    }

    @Test
    fun `stable of older version does not beat alpha of newer version`() {
        assertFalse(isNewer("1.9.9", "2.0.0-alpha01"))
    }

    @Test
    fun `alpha of newer version beats beta of older version`() {
        assertTrue(isNewer("1.1.0-alpha02", "1.0.0-beta01"))
    }

    // Debug stage

    @Test
    fun `alpha is newer than debug (same version numbers)`() {
        assertTrue(isNewer("1.0.0-alpha01", "1.0.0-debug01"))
    }

    @Test
    fun `debug is not newer than alpha (same version numbers)`() {
        assertFalse(isNewer("1.0.0-debug01", "1.0.0-alpha01"))
    }

    @Test
    fun `debug is not newer than stable`() {
        assertFalse(isNewer("1.0.0-debug01", "1.0.0"))
    }

    // Full prerelease lifecycle ordered check

    @Test
    fun `full prerelease lifecycle is ordered correctly`() {
        val versions = listOf(
            "1.0.0-debug01",
            "1.0.0-alpha01",
            "1.0.0-alpha02",
            "1.0.0-beta01",
            "1.0.0-beta02",
            "1.0.0",
        )

        for (i in 1 until versions.size) {
            for (j in 0 until i) {
                assertTrue(
                    "Expected ${versions[i]} > ${versions[j]}, but isNewer returned false",
                    isNewer(versions[i], versions[j])
                )
            }
        }
    }

    // Edge cases

    @Test
    fun `malformed version string does not crash and is not newer`() {
        assertFalse(isNewer("not-a-version", "1.0.0"))
    }

    @Test
    fun `empty string does not crash and is not newer`() {
        assertFalse(isNewer("", "1.0.0"))
    }

    @Test
    fun `version without patch segment is handled`() {
        assertTrue(isNewer("1.1", "1.0"))
    }
}
