package `in`.hridayan.ashell.onboarding.presentation.viewmodel

import androidx.datastore.preferences.core.Preferences
import `in`.hridayan.ashell.core.common.constants.LEGAL_DOCS_VERSION
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeSettingsRepository = FakeSettingsRepository()
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel(fakeSettingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun completeOnboarding_setsLegalDocsVersionAndFirstLaunchFalse() = runTest(testDispatcher) {
        viewModel.completeOnboarding()
        advanceUntilIdle()

        assertEquals(
            LEGAL_DOCS_VERSION,
            fakeSettingsRepository.savedInts[SettingsKeys.SavedLegalDocsVersion]
        )
        assertFalse(
            fakeSettingsRepository.savedBooleans[SettingsKeys.FirstLaunch] ?: true
        )
    }

    private class FakeSettingsRepository : SettingsRepository {
        val savedInts = mutableMapOf<SettingsKeys<Int>, Int>()
        val savedBooleans = mutableMapOf<SettingsKeys<Boolean>, Boolean>()

        override val preferences: Flow<Preferences> = emptyFlow()

        override fun getBoolean(key: SettingsKeys<Boolean>): Flow<Boolean> =
            flowOf(savedBooleans[key] ?: key.default)

        override suspend fun setBoolean(key: SettingsKeys<Boolean>, value: Boolean) {
            savedBooleans[key] = value
        }

        override suspend fun toggleSetting(key: SettingsKeys<Boolean>) {
            val current = savedBooleans[key] ?: key.default
            savedBooleans[key] = !current
        }

        override fun getInt(key: SettingsKeys<Int>): Flow<Int> =
            flowOf(savedInts[key] ?: key.default)

        override suspend fun setInt(key: SettingsKeys<Int>, value: Int) {
            savedInts[key] = value
        }

        override fun getFloat(key: SettingsKeys<Float>): Flow<Float> = flowOf(key.default)

        override suspend fun setFloat(key: SettingsKeys<Float>, value: Float) {}

        override fun getString(key: SettingsKeys<String>): Flow<String> = flowOf(key.default)

        override suspend fun setString(key: SettingsKeys<String>, value: String) {}

        override fun getAllDefaultSettings(): Map<String, Any?> = emptyMap()

        override fun getPreserveKeys(): Set<String> = emptySet()

        override suspend fun getCurrentSettings(): Map<String, Any?> = emptyMap()

        override suspend fun resetAndRestoreDefaults(): Boolean = true
    }
}
