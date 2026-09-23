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

import com.ontheroad.core.model.DirectPricingRates

private class SettingsFakeUserPreferencesRepository : UserPreferencesRepository {
    val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    val directPricingRatesFlow = MutableStateFlow(DirectPricingRates())
    var directPricingRatesWriteCount = 0

    override fun getThemeMode(): Flow<ThemeMode> = themeModeFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeModeFlow.value = mode
    }

    override fun getDirectPricingRates(): Flow<DirectPricingRates> = directPricingRatesFlow

    override suspend fun setDirectPricingRates(rates: DirectPricingRates) {
        directPricingRatesWriteCount++
        directPricingRatesFlow.value = rates
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var preferencesRepository: SettingsFakeUserPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preferencesRepository = SettingsFakeUserPreferencesRepository()
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

    @Test
    fun `directPricingRates state flow emits and updates pricing configurations`() = runTest(testDispatcher) {
        viewModel.directPricingRates.test {
            val initial = awaitItem()
            assertEquals(10_000_00L, initial.baseFareAmountCents)
            assertEquals(3_500_00L, initial.ratePerKmAmountCents)
            assertEquals(15_000_00L, initial.minimumFareAmountCents)

            viewModel.updateBaseFare(12_000_00L)
            testDispatcher.scheduler.runCurrent()
            val afterBase = awaitItem()
            assertEquals(12_000_00L, afterBase.baseFareAmountCents)

            viewModel.updateRatePerKm(4_000_00L)
            testDispatcher.scheduler.runCurrent()
            val afterRate = awaitItem()
            assertEquals(4_000_00L, afterRate.ratePerKmAmountCents)

            viewModel.updateMinimumFare(20_000_00L)
            testDispatcher.scheduler.runCurrent()
            val afterMin = awaitItem()
            assertEquals(20_000_00L, afterMin.minimumFareAmountCents)

            viewModel.updateIncludedBaseDistance(2.0)
            testDispatcher.scheduler.runCurrent()
            val afterDistance = awaitItem()
            assertEquals(2.0, afterDistance.includedBaseDistanceKm, 0.01)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invalid rates are not persisted`() = runTest(testDispatcher) {
        val originalRates = preferencesRepository.directPricingRatesFlow.value
        val invalidRates = listOf(
            originalRates.copy(baseFareAmountCents = -1L),
            originalRates.copy(ratePerKmAmountCents = -1L),
            originalRates.copy(minimumFareAmountCents = -1L),
            originalRates.copy(includedBaseDistanceKm = Double.NaN),
            originalRates.copy(roadDetourMultiplier = Double.POSITIVE_INFINITY),
            originalRates.copy(includedBaseDistanceKm = Double.MAX_VALUE)
        )

        invalidRates.forEach { rates ->
            viewModel.updateDirectPricingRates(rates)
            testDispatcher.scheduler.runCurrent()
        }

        assertEquals(0, preferencesRepository.directPricingRatesWriteCount)
        assertEquals(originalRates, preferencesRepository.directPricingRatesFlow.value)
    }
}
