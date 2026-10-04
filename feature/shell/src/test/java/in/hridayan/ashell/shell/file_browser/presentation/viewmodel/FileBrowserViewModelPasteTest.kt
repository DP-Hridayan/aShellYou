package `in`.hridayan.ashell.shell.file_browser.presentation.viewmodel

import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.file_browser.data.executor.OtgCommandExecutor
import `in`.hridayan.ashell.shell.file_browser.domain.model.ConflictResolution
import `in`.hridayan.ashell.shell.file_browser.domain.usecase.PasteFilesUseCase
import `in`.hridayan.ashell.shell.file_browser.presentation.model.FileBrowserEvent
import `in`.hridayan.ashell.shell.file_browser.testing.DisconnectedOtgRepository
import `in`.hridayan.ashell.shell.file_browser.testing.DisconnectedWifiAdbRepository
import `in`.hridayan.ashell.shell.file_browser.testing.FakeRemoteFileSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val HOME = "/storage/emulated/0"

@OptIn(ExperimentalCoroutinesApi::class)
class FileBrowserViewModelPasteTest {

    private val dispatcher = StandardTestDispatcher()
    private val fs = FakeRemoteFileSystem()
    private lateinit var viewModel: FileBrowserViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        fs.dir(HOME)
        viewModel = FileBrowserViewModel(
            repository = fs,
            wifiAdbRepository = DisconnectedWifiAdbRepository(),
            otgRepository = DisconnectedOtgRepository(),
            otgExecutor = OtgCommandExecutor(DisconnectedOtgRepository()),
            pasteFiles = PasteFilesUseCase(fs)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun givenSecondItemConflicts(): List<String> {
        fs.file("$HOME/A/a.txt", "a")
        fs.file("$HOME/A/b.txt", "new")
        fs.file("$HOME/B/b.txt", "old")
        return listOf("$HOME/A/a.txt", "$HOME/A/b.txt")
    }

    private fun TestScope.collectEvents(): List<FileBrowserEvent> {
        val events = mutableListOf<FileBrowserEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.toList(events)
        }
        return events
    }

    @Test
    fun `dismissing a conflict ends the paste instead of leaving the spinner up`() =
        runTest(dispatcher) {
            viewModel.copyFileBatch(givenSecondItemConflicts(), "$HOME/B")
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.pendingConflict)
            assertTrue(viewModel.state.value.isPasting)

            viewModel.dismissConflict()
            advanceUntilIdle()

            assertFalse(viewModel.state.value.isPasting)
            assertNull(viewModel.state.value.pendingConflict)
            assertNull(viewModel.state.value.pasteProgress)
            assertEquals("a", fs.contentOf("$HOME/B/a.txt"))
            assertEquals("old", fs.contentOf("$HOME/B/b.txt"))
        }

    @Test
    fun `a second paste is refused while the first is still running`() = runTest(dispatcher) {
        val sources = givenSecondItemConflicts()
        assertTrue(viewModel.copyFileBatch(sources, "$HOME/B"))
        advanceUntilIdle()

        assertFalse(viewModel.moveFileBatch(sources, "$HOME/B"))

        viewModel.dismissConflict()
        advanceUntilIdle()
        assertTrue(viewModel.copyFileBatch(listOf("$HOME/A/a.txt"), "$HOME/A"))
    }

    @Test
    fun `answering a conflict continues the paste`() = runTest(dispatcher) {
        viewModel.copyFileBatch(givenSecondItemConflicts(), "$HOME/B")
        advanceUntilIdle()

        viewModel.resolveConflict(ConflictResolution.REPLACE)
        advanceUntilIdle()

        assertEquals("new", fs.contentOf("$HOME/B/b.txt"))
        assertFalse(viewModel.state.value.isPasting)
        assertFalse(fs.hasTemporaryLeftovers())
    }

    @Test
    fun `cancelling while a conflict is open stops the paste and says so`() = runTest(dispatcher) {
        val events = collectEvents()
        viewModel.copyFileBatch(givenSecondItemConflicts(), "$HOME/B")
        advanceUntilIdle()

        viewModel.cancelPaste()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isPasting)
        assertEquals("old", fs.contentOf("$HOME/B/b.txt"))
        assertTrue(events.contains(FileBrowserEvent.ShowToast(R.string.fb_operation_cancelled)))
    }

    @Test
    fun `an empty clipboard is refused`() = runTest(dispatcher) {
        assertFalse(viewModel.copyFileBatch(emptyList(), HOME))
    }

    @Test
    fun `the counts and the reason of a failure are announced`() = runTest(dispatcher) {
        val events = collectEvents()
        fs.file("$HOME/A/sub/f.txt", "f")

        viewModel.copyFileBatch(listOf("$HOME/A"), "$HOME/A/sub")
        advanceUntilIdle()

        assertTrue(
            events.contains(
                FileBrowserEvent.ShowToast(R.string.fb_paste_completed_with_failed, listOf(0, 0, 1))
            )
        )
        assertTrue(
            events.contains(FileBrowserEvent.ShowToast(R.string.fb_paste_into_itself, listOf("A")))
        )
    }
}
