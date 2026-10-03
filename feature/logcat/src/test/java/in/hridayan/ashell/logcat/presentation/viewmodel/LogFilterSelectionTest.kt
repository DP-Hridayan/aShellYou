package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.repository.PackageUidResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val errors = LogFilter(id = "errors", name = "Errors")
private val network = LogFilter(id = "network", name = "Network")
private val chrome = LogFilter(id = "chrome", name = "Chrome", packages = setOf("com.android.chrome"))

/** A device on which only the given packages are installed. */
private class FakeResolver(private val uids: Map<String, String>) : PackageUidResolver {
    var lookups = 0

    override suspend fun uidsOf(packages: Set<String>): Map<String, String> {
        lookups++
        return uids.filterKeys { it in packages }
    }

    override suspend fun packagesOf(uid: String): List<String> =
        uids.filterValues { it == uid }.keys.sorted()
}

@OptIn(ExperimentalCoroutinesApi::class)
class LogFilterSelectionTest {

    @Test
    fun `criteria holds only the active profiles`() = runTest {
        val repository = FakeFilterRepository(listOf(errors, network), activeIds = setOf("network"))
        val selection = LogFilterSelection(repository, backgroundScope)
        runCurrent()
        assertEquals(listOf(network), selection.criteria.value.activeProfiles)
    }

    @Test
    fun `toggling activates and deactivates a profile`() = runTest {
        val repository = FakeFilterRepository(listOf(errors, network))
        val selection = LogFilterSelection(repository, backgroundScope)
        selection.toggle("errors")
        runCurrent()
        assertEquals(setOf("errors"), selection.activeProfileIds.value)
        selection.toggle("errors")
        runCurrent()
        assertTrue(selection.activeProfileIds.value.isEmpty())
    }

    @Test
    fun `two quick toggles both take effect`() = runTest {
        val repository = FakeFilterRepository(listOf(errors, network))
        val selection = LogFilterSelection(repository, backgroundScope)
        selection.toggle("errors")
        selection.toggle("network")
        runCurrent()
        assertEquals(setOf("errors", "network"), selection.activeProfileIds.value)
    }

    @Test
    fun `deleting an active profile removes it from the criteria`() = runTest {
        val repository = FakeFilterRepository(listOf(errors, network), activeIds = setOf("errors"))
        val selection = LogFilterSelection(repository, backgroundScope)
        selection.delete("errors")
        runCurrent()
        assertEquals(listOf(network), selection.profiles.value)
        assertTrue(selection.criteria.value.activeProfiles.isEmpty())
    }

    @Test
    fun `the search query is part of the criteria`() = runTest {
        val selection = LogFilterSelection(FakeFilterRepository(), backgroundScope)
        selection.search("binder")
        runCurrent()
        assertEquals("binder", selection.criteria.value.searchQuery)
    }

    @Test
    fun `a failed toggle leaves the selection unchanged`() = runTest {
        val repository = FakeFilterRepository(listOf(errors)).apply { failure = IllegalStateException() }
        val selection = LogFilterSelection(repository, backgroundScope)
        selection.toggle("errors")
        runCurrent()
        assertTrue(selection.activeProfileIds.value.isEmpty())
    }

    @Test
    fun `a tab's criteria carry the package uids of its own device`() = runTest {
        val repository = FakeFilterRepository(listOf(chrome), activeIds = setOf("chrome"))
        val selection = LogFilterSelection(repository, backgroundScope)
        val phone = selection.criteriaFor(flowOf(FakeResolver(mapOf("com.android.chrome" to "10077"))))
        val tablet = selection.criteriaFor(flowOf(FakeResolver(mapOf("com.android.chrome" to "10123"))))
        runCurrent()
        assertEquals(mapOf("com.android.chrome" to "10077"), phone.value.packageUids)
        assertEquals(mapOf("com.android.chrome" to "10123"), tablet.value.packageUids)
    }

    @Test
    fun `no device means no package uids`() = runTest {
        val repository = FakeFilterRepository(listOf(chrome), activeIds = setOf("chrome"))
        val selection = LogFilterSelection(repository, backgroundScope)
        val criteria = selection.criteriaFor(flowOf(null))
        runCurrent()
        assertEquals(listOf(chrome), criteria.value.activeProfiles)
        assertTrue(criteria.value.packageUids.isEmpty())
    }

    @Test
    fun `switching device re-resolves the packages`() = runTest {
        val repository = FakeFilterRepository(listOf(chrome), activeIds = setOf("chrome"))
        val selection = LogFilterSelection(repository, backgroundScope)
        val device = MutableStateFlow<PackageUidResolver?>(null)
        val criteria = selection.criteriaFor(device)
        runCurrent()
        device.value = FakeResolver(mapOf("com.android.chrome" to "10077"))
        runCurrent()
        assertEquals(mapOf("com.android.chrome" to "10077"), criteria.value.packageUids)
    }

    @Test
    fun `profiles without packages skip the lookup`() = runTest {
        val repository = FakeFilterRepository(listOf(errors), activeIds = setOf("errors"))
        val selection = LogFilterSelection(repository, backgroundScope)
        val resolver = FakeResolver(emptyMap())
        selection.criteriaFor(flowOf(resolver))
        runCurrent()
        assertEquals(0, resolver.lookups)
    }
}
