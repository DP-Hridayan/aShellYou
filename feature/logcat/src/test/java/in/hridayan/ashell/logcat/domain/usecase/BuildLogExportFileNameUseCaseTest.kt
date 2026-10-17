package `in`.hridayan.ashell.logcat.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class BuildLogExportFileNameUseCaseTest {

    private val buildFileName = BuildLogExportFileNameUseCase()

    @Test
    fun `the name carries the app, the feature and the moment of export`() {
        val now = LocalDateTime.of(2026, 10, 17, 14, 2, 11)
        assertEquals("aShellYou_logcat_20261017_140211.txt", buildFileName(now))
    }

    @Test
    fun `single digit fields are zero padded`() {
        val now = LocalDateTime.of(2026, 1, 2, 3, 4, 5)
        assertEquals("aShellYou_logcat_20260102_030405.txt", buildFileName(now))
    }
}
