package com.ontheroad.viewmodel

import app.cash.turbine.test
import com.ontheroad.core.domain.repository.UserPreferencesRepository
import com.ontheroad.core.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private class TestFakeUserPreferencesRepository : UserPreferencesRepository {
    val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)

    override fun getThemeMode(): Flow<ThemeMode> = themeModeFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeModeFlow.value = mode
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var preferencesRepository: TestFakeUserPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepository = TestFakeUserPreferencesRepository()
        viewModel = SettingsViewModel(preferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `themeMode state flow emits current preference`() = runTest(testDispatcher) {
        viewModel.themeMode.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())

            viewModel.setThemeMode(ThemeMode.DARK)
            testDispatcher.scheduler.runCurrent()
            assertEquals(ThemeMode.DARK, awaitItem())

            viewModel.setThemeMode(ThemeMode.LIGHT)
            testDispatcher.scheduler.runCurrent()
            assertEquals(ThemeMode.LIGHT, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
