package `in`.hridayan.ashell.logcat.presentation.model

import `in`.hridayan.ashell.logcat.domain.model.DefaultIncludeLevels
import `in`.hridayan.ashell.logcat.domain.model.FilterMode
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelSummaryTest {

    @Test
    fun `include lists the selected levels in severity order`() {
        val profile = LogFilter(levels = setOf(LogLevel.ERROR, LogLevel.DEBUG))
        assertEquals(LevelSummary.Only(listOf(LogLevel.DEBUG, LogLevel.ERROR)), profile.levelSummary())
    }

    @Test
    fun `include with every level or none reads as all levels`() {
        assertEquals(LevelSummary.AllLevels, LogFilter(levels = SelectableLogLevels.toSet()).levelSummary())
        assertEquals(LevelSummary.AllLevels, LogFilter(levels = emptySet()).levelSummary())
    }

    @Test
    fun `exclude reads as all except the hidden levels`() {
        val profile = LogFilter(mode = FilterMode.EXCLUDE, levels = setOf(LogLevel.VERBOSE))
        assertEquals(LevelSummary.AllExcept(listOf(LogLevel.VERBOSE)), profile.levelSummary())
    }

    @Test
    fun `exclude with nothing hidden reads as all levels`() {
        val profile = LogFilter(mode = FilterMode.EXCLUDE, levels = emptySet())
        assertEquals(LevelSummary.AllLevels, profile.levelSummary())
    }
}

class FilterProfileFormTest {

    @Test
    fun `a blank name is not valid`() {
        assertFalse(FilterProfileForm(name = "   ").isValid)
        assertTrue(FilterProfileForm(name = "Errors").isValid)
    }

    @Test
    fun `multi-value fields keep exactly what was typed`() {
        val form = FilterProfileForm().copy(tags = "ActivityManager, ")
        assertEquals("ActivityManager, ", form.tags)
    }

    @Test
    fun `saving splits values on commas and drops blanks`() {
        val profile = FilterProfileForm(
            name = " Errors ",
            tags = "ActivityManager, , Zygote ,",
            pids = "100,200",
            tids = "",
        ).toProfile(id = "p1")
        assertEquals("p1", profile.id)
        assertEquals("Errors", profile.name)
        assertEquals(setOf("ActivityManager", "Zygote"), profile.tags)
        assertEquals(setOf("100", "200"), profile.pids)
        assertTrue(profile.tids.isEmpty())
    }

    @Test
    fun `a saved profile round-trips through the form`() {
        val profile = LogFilter(
            id = "p1",
            name = "Mine",
            levels = setOf(LogLevel.WARNING),
            pids = setOf("1", "2"),
            tags = setOf("Foo"),
            packages = setOf("com.android.chrome", "com.android.phone"),
            mode = FilterMode.EXCLUDE,
        )
        assertEquals(profile, FilterProfileForm.from(profile).toProfile(profile.id))
    }

    @Test
    fun `switching mode resets the levels to that mode's default`() {
        val form = FilterProfileForm(levels = setOf(LogLevel.ERROR))
        assertTrue(form.withMode(FilterMode.EXCLUDE).levels.isEmpty())
        assertEquals(DefaultIncludeLevels, form.withMode(FilterMode.EXCLUDE).withMode(FilterMode.INCLUDE).levels)
    }

    @Test
    fun `choosing the current mode keeps the levels`() {
        val form = FilterProfileForm(levels = setOf(LogLevel.ERROR))
        assertEquals(setOf(LogLevel.ERROR), form.withMode(FilterMode.INCLUDE).levels)
    }

    @Test
    fun `toggling a level adds or removes it`() {
        val form = FilterProfileForm(levels = setOf(LogLevel.ERROR))
        assertEquals(setOf(LogLevel.ERROR, LogLevel.INFO), form.withLevelToggled(LogLevel.INFO).levels)
        assertTrue(form.withLevelToggled(LogLevel.ERROR).levels.isEmpty())
    }

    @Test
    fun `saving splits packages like the other fields`() {
        val profile = FilterProfileForm(name = "Apps", packages = "com.a, com.b,").toProfile("p1")
        assertEquals(setOf("com.a", "com.b"), profile.packages)
    }

    @Test
    fun `picking an app adds its package after the ones already there`() {
        val form = FilterProfileForm(packages = "com.a").withPackageToggled("com.b")
        assertEquals("com.a, com.b", form.packages)
        assertEquals(setOf("com.a", "com.b"), form.selectedPackages)
    }

    @Test
    fun `picking a selected app removes only its package`() {
        val form = FilterProfileForm(packages = "com.a, com.b, com.c").withPackageToggled("com.b")
        assertEquals("com.a, com.c", form.packages)
    }

    @Test
    fun `picking into an empty field gives just that package`() {
        assertEquals("com.a", FilterProfileForm().withPackageToggled("com.a").packages)
    }
}
