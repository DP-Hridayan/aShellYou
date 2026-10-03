package `in`.hridayan.ashell.logcat.presentation.viewmodel

import `in`.hridayan.ashell.logcat.domain.model.InstalledApp
import `in`.hridayan.ashell.logcat.domain.model.LogFilter
import `in`.hridayan.ashell.logcat.domain.model.LogLevel
import `in`.hridayan.ashell.logcat.domain.repository.InstalledAppsRepository
import `in`.hridayan.ashell.logcat.presentation.event.FilterEditorEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private val existing = LogFilter(id = "p1", name = "Errors", levels = setOf(LogLevel.ERROR))
private val other = LogFilter(id = "p2", name = "Other")
private val chromeApp = InstalledApp("com.android.chrome", "Chrome")
private val phoneApp = InstalledApp("com.android.phone", "Phone Services")

/** Serves a fixed app list and counts how often it was asked. */
private class FakeAppsRepository(
    private val result: Result<List<InstalledApp>> = Result.success(listOf(chromeApp, phoneApp)),
) : InstalledAppsRepository {
    var requests = 0

    override suspend fun installedApps(): Result<List<InstalledApp>> {
        requests++
        return result
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LogcatFilterEditorViewModelTest {

    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun editorFor(
        profileId: String?,
        repository: FakeFilterRepository,
        apps: FakeAppsRepository = FakeAppsRepository(),
    ) = LogcatFilterEditorViewModel(profileId = profileId, repository = repository, installedApps = apps)

    @Test
    fun `editing loads the saved profile into the form`() = runTest(dispatcher) {
        val editor = editorFor("p1", FakeFilterRepository(listOf(existing)))
        advanceUntilIdle()
        assertTrue(editor.isEditing)
        assertEquals("Errors", editor.form.value.name)
        assertEquals(setOf(LogLevel.ERROR), editor.form.value.levels)
    }

    @Test
    fun `saving with a blank name shows the error and saves nothing`() = runTest(dispatcher) {
        val repository = FakeFilterRepository()
        val editor = editorFor(null, repository)
        editor.save()
        advanceUntilIdle()
        assertTrue(editor.form.value.showNameError)
        assertTrue(repository.profiles.value.isEmpty())
    }

    @Test
    fun `a new profile is saved and made active`() = runTest(dispatcher) {
        val repository = FakeFilterRepository()
        val editor = editorFor(null, repository)
        assertFalse(editor.isEditing)
        editor.onNameChange("Network")
        editor.onTagsChange("OkHttp, Volley,")
        val event = backgroundScope.async { editor.events.first() }
        editor.save()
        advanceUntilIdle()
        val saved = repository.profiles.value.single()
        assertEquals("Network", saved.name)
        assertEquals(setOf("OkHttp", "Volley"), saved.tags)
        assertEquals(setOf(saved.id), repository.activeIds.value)
        assertEquals(FilterEditorEvent.Saved, event.await())
    }

    @Test
    fun `an edited profile keeps its place and its active state`() = runTest(dispatcher) {
        val repository = FakeFilterRepository(listOf(existing, other), activeIds = emptySet())
        val editor = editorFor("p1", repository)
        advanceUntilIdle()
        editor.onNameChange("Crashes")
        editor.save()
        advanceUntilIdle()
        assertEquals(listOf("p1", "p2"), repository.profiles.value.map { it.id })
        assertEquals("Crashes", repository.profiles.value.first().name)
        assertTrue(repository.activeIds.value.isEmpty())
    }

    @Test
    fun `a failed save is reported`() = runTest(dispatcher) {
        val repository = FakeFilterRepository().apply { failure = IllegalStateException() }
        val editor = editorFor(null, repository)
        editor.onNameChange("Network")
        val event = backgroundScope.async { editor.events.first() }
        editor.save()
        advanceUntilIdle()
        assertEquals(FilterEditorEvent.SaveFailed, event.await())
    }

    @Test
    fun `installed apps are loaded once, however often the picker opens`() = runTest(dispatcher) {
        val apps = FakeAppsRepository()
        val editor = editorFor(null, FakeFilterRepository(), apps)
        editor.loadInstalledApps()
        editor.loadInstalledApps()
        advanceUntilIdle()
        assertEquals(listOf(chromeApp, phoneApp), editor.visibleApps.value)
        assertEquals(1, apps.requests)
    }

    @Test
    fun `the picker search matches app name or package`() = runTest(dispatcher) {
        val editor = editorFor(null, FakeFilterRepository())
        editor.loadInstalledApps()
        editor.onAppQueryChange("phone")
        advanceUntilIdle()
        assertEquals(listOf(phoneApp), editor.visibleApps.value)
        editor.onAppQueryChange("CHROME")
        advanceUntilIdle()
        assertEquals(listOf(chromeApp), editor.visibleApps.value)
    }

    @Test
    fun `a failed app list shows an empty picker rather than loading forever`() = runTest(dispatcher) {
        val editor = editorFor(null, FakeFilterRepository(), FakeAppsRepository(Result.failure(SecurityException())))
        editor.loadInstalledApps()
        advanceUntilIdle()
        assertEquals(emptyList<InstalledApp>(), editor.visibleApps.value)
    }

    @Test
    fun `picking apps fills the packages field`() = runTest(dispatcher) {
        val editor = editorFor(null, FakeFilterRepository())
        editor.onPackageToggle("com.android.chrome")
        editor.onPackageToggle("com.android.phone")
        editor.onPackageToggle("com.android.chrome")
        assertEquals("com.android.phone", editor.form.value.packages)
    }
}
