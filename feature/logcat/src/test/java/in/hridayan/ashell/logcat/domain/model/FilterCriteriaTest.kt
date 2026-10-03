package `in`.hridayan.ashell.logcat.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private fun entry(
    level: LogLevel = LogLevel.INFO,
    tag: String = "Foo",
    message: String = "hello world",
    uid: String = "",
) = LogEntry(
    id = 1,
    timestamp = "10-03 10:00:00.000",
    pid = "100",
    tid = "200",
    uid = uid,
    packageName = "",
    level = level,
    tag = tag,
    message = message,
)

private val includeVdi = LogFilter(
    mode = FilterMode.INCLUDE,
    levels = setOf(LogLevel.VERBOSE, LogLevel.DEBUG, LogLevel.INFO),
)
private val excludeVerbose = LogFilter(mode = FilterMode.EXCLUDE, levels = setOf(LogLevel.VERBOSE))

class FilterCriteriaTest {

    @Test
    fun `with no active profile every log is shown, verbose included`() {
        assertTrue(FilterCriteria().matches(entry(LogLevel.VERBOSE)))
    }

    @Test
    fun `a single profile decides on its own`() {
        assertFalse(FilterCriteria(listOf(excludeVerbose)).matches(entry(LogLevel.VERBOSE)))
        assertTrue(FilterCriteria(listOf(includeVdi)).matches(entry(LogLevel.VERBOSE)))
    }

    @Test
    fun `a log is shown when any active profile shows it`() {
        val both = FilterCriteria(listOf(excludeVerbose, includeVdi))
        assertTrue(both.matches(entry(LogLevel.VERBOSE)))
    }

    @Test
    fun `each profile is evaluated whole rather than merged field by field`() {
        val barOnly = LogFilter(mode = FilterMode.INCLUDE, levels = emptySet(), tags = setOf("Bar"))
        val excludeError = LogFilter(mode = FilterMode.EXCLUDE, levels = setOf(LogLevel.ERROR))
        val criteria = FilterCriteria(listOf(barOnly, excludeError))
        assertTrue(criteria.matches(entry(LogLevel.VERBOSE, tag = "Foo")))
        assertFalse(criteria.matches(entry(LogLevel.ERROR, tag = "Foo")))
        assertTrue(criteria.matches(entry(LogLevel.ERROR, tag = "Bar")))
    }

    @Test
    fun `an include profile with no levels selected does not restrict levels`() {
        val anyLevel = LogFilter(mode = FilterMode.INCLUDE, levels = emptySet())
        assertTrue(FilterCriteria(listOf(anyLevel)).matches(entry(LogLevel.VERBOSE)))
    }

    @Test
    fun `search narrows the logs the profiles show`() {
        val criteria = FilterCriteria(listOf(includeVdi), searchQuery = "WORLD")
        assertTrue(criteria.matches(entry(message = "hello world")))
        assertFalse(criteria.matches(entry(message = "goodbye")))
    }

    @Test
    fun `search also matches the tag`() {
        assertTrue(FilterCriteria(searchQuery = "fo").matches(entry(tag = "Foo", message = "x")))
    }

    @Test
    fun `a blank search matches everything`() {
        assertTrue(FilterCriteria(searchQuery = "   ").matches(entry()))
    }

    @Test
    fun `pid and tid lists restrict an include profile`() {
        val pidOnly = LogFilter(mode = FilterMode.INCLUDE, levels = emptySet(), pids = setOf("999"))
        assertFalse(FilterCriteria(listOf(pidOnly)).matches(entry()))
    }

    @Test
    fun `pid and tid lists hide matching logs in an exclude profile`() {
        val hideTid = LogFilter(mode = FilterMode.EXCLUDE, levels = emptySet(), tids = setOf("200"))
        assertFalse(FilterCriteria(listOf(hideTid)).matches(entry()))
    }

    @Test
    fun `an include profile with a package shows only that app's uid`() {
        val chrome = LogFilter(
            mode = FilterMode.INCLUDE,
            levels = emptySet(),
            packages = setOf("com.android.chrome"),
        )
        val criteria = FilterCriteria(listOf(chrome), packageUids = mapOf("com.android.chrome" to "10077"))
        assertTrue(criteria.matches(entry(uid = "10077")))
        assertFalse(criteria.matches(entry(uid = "10088")))
        assertFalse(criteria.matches(entry(uid = "")))
    }

    @Test
    fun `an exclude profile with a package hides that app's uid`() {
        val hideChrome = LogFilter(
            mode = FilterMode.EXCLUDE,
            levels = emptySet(),
            packages = setOf("com.android.chrome"),
        )
        val criteria = FilterCriteria(listOf(hideChrome), packageUids = mapOf("com.android.chrome" to "10077"))
        assertFalse(criteria.matches(entry(uid = "10077")))
        assertTrue(criteria.matches(entry(uid = "10088")))
    }

    @Test
    fun `a package that is not installed matches nothing on that device`() {
        val missing = LogFilter(mode = FilterMode.INCLUDE, levels = emptySet(), packages = setOf("com.missing"))
        assertFalse(FilterCriteria(listOf(missing)).matches(entry(uid = "10077")))
    }

    @Test
    fun `any listed package is enough to include a log`() {
        val two = LogFilter(mode = FilterMode.INCLUDE, levels = emptySet(), packages = setOf("a", "b"))
        val criteria = FilterCriteria(listOf(two), packageUids = mapOf("a" to "10001", "b" to "10002"))
        assertTrue(criteria.matches(entry(uid = "10002")))
    }

    @Test
    fun `the criteria list every package its active profiles name`() {
        val first = LogFilter(packages = setOf("a"))
        val second = LogFilter(packages = setOf("b", "a"))
        assertEquals(setOf("a", "b"), FilterCriteria(listOf(first, second)).packages)
    }
}
