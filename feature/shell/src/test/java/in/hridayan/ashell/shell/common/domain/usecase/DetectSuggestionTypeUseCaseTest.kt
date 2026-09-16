package `in`.hridayan.ashell.shell.common.domain.usecase

import `in`.hridayan.ashell.shell.common.domain.model.SuggestionType
import org.junit.Assert.assertEquals
import org.junit.Test

class DetectSuggestionTypeUseCaseTest {

    private val detect = DetectSuggestionTypeUseCase()

    @Test
    fun `blank input asks for commands`() {
        assertEquals(SuggestionType.COMMAND, detect("").suggestionType)
    }

    @Test
    fun `a plain command asks for commands`() {
        val context = detect("pm gra")
        assertEquals(SuggestionType.COMMAND, context.suggestionType)
        assertEquals("pm gra", context.filterPrefix)
    }

    @Test
    fun `a dotted token asks for packages`() {
        val context = detect("pm uninstall com.exam")
        assertEquals(SuggestionType.PACKAGE, context.suggestionType)
        assertEquals("com.exam", context.filterPrefix)
    }

    @Test
    fun `an android prefixed token asks for permissions`() {
        val context = detect("pm grant x android.permission.CAM")
        assertEquals(SuggestionType.PERMISSION, context.suggestionType)
    }

    @Test
    fun `the adb token alone asks for adb commands`() {
        val context = detect("adb")
        assertEquals(SuggestionType.ADB, context.suggestionType)
        assertEquals("adb", context.filterPrefix)
    }

    @Test
    fun `a partial adb subcommand asks for adb commands`() {
        val context = detect("adb t")
        assertEquals(SuggestionType.ADB, context.suggestionType)
        assertEquals("adb t", context.filterPrefix)
    }

    @Test
    fun `an address after adb connect is not mistaken for a package`() {
        assertEquals(SuggestionType.ADB, detect("adb connect 192.168.1.5").suggestionType)
    }

    @Test
    fun `a word merely starting with adb is not an adb command`() {
        assertEquals(SuggestionType.COMMAND, detect("adbd").suggestionType)
    }

    @Test
    fun `adb shell delegates so package suggestions still work`() {
        val context = detect("adb shell pm uninstall com.exam")
        assertEquals(SuggestionType.PACKAGE, context.suggestionType)
        assertEquals("com.exam", context.filterPrefix)
    }

    @Test
    fun `adb shell delegates so command suggestions still work`() {
        val context = detect("adb shell pm gra")
        assertEquals(SuggestionType.COMMAND, context.suggestionType)
        assertEquals("pm gra", context.filterPrefix)
    }

    @Test
    fun `bare adb shell still asks for adb commands`() {
        assertEquals(SuggestionType.ADB, detect("adb shell").suggestionType)
    }

    @Test
    fun `adb exec-out delegates to the remainder`() {
        assertEquals(SuggestionType.COMMAND, detect("adb exec-out screenc").suggestionType)
    }
}
