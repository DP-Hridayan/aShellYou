package `in`.hridayan.ashell.commandexamples.presentation.viewmodel

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.text.input.TextFieldValue
import androidx.datastore.preferences.core.Preferences
import `in`.hridayan.ashell.core.common.domain.model.CommandEntity
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.model.ai.AnalysisResult
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.repository.AiAnalysisRepository
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.repository.CommandRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.domain.usecase.ai.AnalyzeCommandUseCase
import `in`.hridayan.ashell.core.common.domain.usecase.ai.GetActiveLlmProviderUseCase
import `in`.hridayan.ashell.core.common.domain.usecase.ai.RequireActiveProviderKeyUseCase
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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

private class FakeTestContext : ContextWrapper(null)

private class FakeCommandRepository(
    initialCommands: List<CommandEntity> = emptyList()
) : CommandRepository {
    val commands = initialCommands.toMutableList()

    override suspend fun insertCommand(command: CommandEntity) {
        commands.add(command)
    }

    override suspend fun insertAllCommands(commands: List<CommandEntity>) {
        this.commands.addAll(commands)
    }

    override fun loadDefaultCommandsWithProgress(): Flow<Float> = emptyFlow()

    override suspend fun updateCommand(command: CommandEntity) {
        val index = commands.indexOfFirst { it.id == command.id }
        if (index != -1) commands[index] = command
    }

    override suspend fun deleteCommand(id: Int) {
        commands.removeAll { it.id == id }
    }

    override suspend fun deleteAllCommands() {
        commands.clear()
    }

    override fun getCommandCount(): Int = commands.size

    override suspend fun getAllCommandsOnce(): List<CommandEntity> = commands.toList()

    override fun getAllLabels(): Flow<List<String>> = flowOf(emptyList())

    override suspend fun getCommandById(id: Int): CommandEntity? = commands.find { it.id == id }

    override fun getSortedCommands(sortType: Int): Flow<List<CommandEntity>> = flowOf(commands)

    override fun getFavoriteCommands(): Flow<List<CommandEntity>> = flowOf(emptyList())

    override fun searchCommands(query: String): Flow<List<CommandEntity>> = flowOf(emptyList())

    override suspend fun incrementUseCount(commandId: Int) {}

    override suspend fun updateFavoriteStatus(commandId: Int, isFavourite: Boolean) {}

    override suspend fun doesCommandExist(command: String): Boolean {
        return commands.any { it.command == command }
    }
}

private class FakeSettingsRepository : SettingsRepository {
    override val preferences: Flow<Preferences> = emptyFlow()
    override fun getBoolean(key: SettingsKeys<Boolean>): Flow<Boolean> = flowOf(key.default)
    override suspend fun setBoolean(key: SettingsKeys<Boolean>, value: Boolean) {}
    override suspend fun toggleSetting(key: SettingsKeys<Boolean>) {}
    override fun getInt(key: SettingsKeys<Int>): Flow<Int> = flowOf(key.default)
    override suspend fun setInt(key: SettingsKeys<Int>, value: Int) {}
    override fun getFloat(key: SettingsKeys<Float>): Flow<Float> = flowOf(key.default)
    override suspend fun setFloat(key: SettingsKeys<Float>, value: Float) {}
    override fun getString(key: SettingsKeys<String>): Flow<String> = flowOf(key.default)
    override suspend fun setString(key: SettingsKeys<String>, value: String) {}
    override fun getAllDefaultSettings(): Map<String, Any?> = emptyMap()
    override fun getPreserveKeys(): Set<String> = emptySet()
    override suspend fun getCurrentSettings(): Map<String, Any?> = emptyMap()
    override suspend fun resetAndRestoreDefaults(): Boolean = true
}

private class FakeAiAnalysisRepository : AiAnalysisRepository {
    override suspend fun analyzeCommand(
        command: String,
        ragContext: String,
        fallbackModels: List<String>?
    ): AnalysisResult {
        throw UnsupportedOperationException()
    }

    override suspend fun queryCommand(
        query: String,
        tools: List<AiTool>,
        fallbackModels: List<String>?
    ): AnalysisResult {
        throw UnsupportedOperationException()
    }

    override suspend fun generateRawCompletion(prompt: String, maxTokens: Int): String {
        throw UnsupportedOperationException()
    }

    override suspend fun getCachedAnalysis(command: String): AnalysisResult? = null

    override suspend fun clearCache() {}

    override suspend fun getCacheSizeBytes(): Long = 0L
}

private class FakeApiKeyRepository : ApiKeyRepository {
    override fun getKey(provider: LlmProvider): String? = null
    override fun setKey(provider: LlmProvider, key: String) {}
    override fun deleteKey(provider: LlmProvider) {}
    override fun hasKey(provider: LlmProvider): Flow<Boolean> = flowOf(false)
}

@OptIn(ExperimentalCoroutinesApi::class)
class CommandExamplesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val fakeContext: Context = FakeTestContext()
    private val fakeSettingsRepository = FakeSettingsRepository()
    private val fakeAiAnalysisRepository = FakeAiAnalysisRepository()
    private val fakeApiKeyRepository = FakeApiKeyRepository()

    private lateinit var commandRepository: FakeCommandRepository
    private lateinit var viewModel: CommandExamplesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        commandRepository = FakeCommandRepository(
            listOf(
                CommandEntity(id = 1, command = "pm list packages", description = "List all packages")
            )
        )
        val analyzeCommandUseCase = AnalyzeCommandUseCase(fakeAiAnalysisRepository, commandRepository)
        val getActiveLlmProvider = GetActiveLlmProviderUseCase(fakeSettingsRepository)
        val requireActiveProviderKey = RequireActiveProviderKeyUseCase(getActiveLlmProvider, fakeApiKeyRepository)

        viewModel = CommandExamplesViewModel(
            commandRepository = commandRepository,
            settingsRepository = fakeSettingsRepository,
            analyzeCommandUseCase = analyzeCommandUseCase,
            appContext = fakeContext,
            requireActiveProviderKey = requireActiveProviderKey
        )
        viewModel.stringResolver = { resId ->
            when (resId) {
                R.string.command_already_exists -> "Command already exists"
                R.string.field_cannot_be_blank -> "Field cannot be blank"
                else -> "string_$resId"
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `entering existing command in add mode marks field as error`() = runTest(dispatcher) {
        viewModel.onCommandFieldTextChange(TextFieldValue("pm list packages"), isNewCommand = true)
        advanceUntilIdle()

        val state = viewModel.states.value.commandField
        assertTrue(state.isError)
        assertEquals("Command already exists", state.errorMessage)
    }

    @Test
    fun `entering existing command with whitespace in add mode marks field as error`() = runTest(dispatcher) {
        viewModel.onCommandFieldTextChange(TextFieldValue("  pm list packages  "), isNewCommand = true)
        advanceUntilIdle()

        val state = viewModel.states.value.commandField
        assertTrue(state.isError)
        assertEquals("Command already exists", state.errorMessage)
    }

    @Test
    fun `entering unique command in add mode does not mark field as error`() = runTest(dispatcher) {
        viewModel.onCommandFieldTextChange(TextFieldValue("pm install app.apk"), isNewCommand = true)
        advanceUntilIdle()

        val state = viewModel.states.value.commandField
        assertFalse(state.isError)
        assertEquals("", state.errorMessage)
    }

    @Test
    fun `addCommand prevents insertion when command is duplicate`() = runTest(dispatcher) {
        viewModel.onCommandFieldTextChange(TextFieldValue("pm list packages"), isNewCommand = true)
        viewModel.onDescriptionFieldTextChange(TextFieldValue("Another description"))
        advanceUntilIdle()

        var onSuccessCalled = false
        viewModel.addCommand(onSuccess = { onSuccessCalled = true })
        advanceUntilIdle()

        assertFalse(onSuccessCalled)
        assertEquals(1, commandRepository.commands.size)
    }

    @Test
    fun `hasActiveAiProviderKey reflects key availability`() = runTest(dispatcher) {
        assertFalse(viewModel.hasActiveAiProviderKey.value)
    }
}
