package com.ontheroad.core.data.repository

import android.content.SharedPreferences
import app.cash.turbine.test
import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.model.ThemeMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserPreferencesRepositoryTest {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var repository: UserPreferencesRepositoryImpl

    @Before
    fun setUp() {
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
    }

    @Test
    fun `initializes with saved theme mode`() = runTest {
        every { sharedPreferences.getString(UserPreferencesRepositoryImpl.KEY_THEME_MODE, any()) } returns "DARK"
        repository = UserPreferencesRepositoryImpl(sharedPreferences)

        repository.getThemeMode().test {
            assertEquals(ThemeMode.DARK, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `defaults to SYSTEM when no saved theme mode`() = runTest {
        every { sharedPreferences.getString(UserPreferencesRepositoryImpl.KEY_THEME_MODE, any()) } returns null
        repository = UserPreferencesRepositoryImpl(sharedPreferences)

        repository.getThemeMode().test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setThemeMode persists to SharedPreferences and updates flow`() = runTest {
        every { sharedPreferences.getString(UserPreferencesRepositoryImpl.KEY_THEME_MODE, any()) } returns "SYSTEM"
        repository = UserPreferencesRepositoryImpl(sharedPreferences)

        repository.getThemeMode().test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())

            repository.setThemeMode(ThemeMode.LIGHT)
            assertEquals(ThemeMode.LIGHT, awaitItem())

            repository.setThemeMode(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        verify { editor.putString(UserPreferencesRepositoryImpl.KEY_THEME_MODE, "LIGHT") }
        verify { editor.putString(UserPreferencesRepositoryImpl.KEY_THEME_MODE, "DARK") }
    }

    @Test
    fun `initializes with default direct pricing rates when none saved`() = runTest {
        every { sharedPreferences.getLong(UserPreferencesRepositoryImpl.KEY_BASE_FARE_CENTS, any()) } returns UserPreferencesRepositoryImpl.DEFAULT_BASE_FARE_CENTS
        every { sharedPreferences.getLong(UserPreferencesRepositoryImpl.KEY_RATE_PER_KM_CENTS, any()) } returns UserPreferencesRepositoryImpl.DEFAULT_RATE_PER_KM_CENTS
        every { sharedPreferences.getLong(UserPreferencesRepositoryImpl.KEY_MIN_FARE_CENTS, any()) } returns UserPreferencesRepositoryImpl.DEFAULT_MIN_FARE_CENTS
        every { sharedPreferences.getFloat(UserPreferencesRepositoryImpl.KEY_INCLUDED_BASE_KM, any()) } returns 0.0f
        every { sharedPreferences.getFloat(UserPreferencesRepositoryImpl.KEY_ROAD_DETOUR_MULTIPLIER, any()) } returns 1.25f

        repository = UserPreferencesRepositoryImpl(sharedPreferences)

        repository.getDirectPricingRates().test {
            val rates = awaitItem()
            assertEquals(10_000_00L, rates.baseFareAmountCents)
            assertEquals(3_500_00L, rates.ratePerKmAmountCents)
            assertEquals(15_000_00L, rates.minimumFareAmountCents)
            assertEquals(0.0, rates.includedBaseDistanceKm, 0.01)
            assertEquals(1.25, rates.roadDetourMultiplier, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setDirectPricingRates persists to SharedPreferences and updates flow`() = runTest {
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putFloat(any(), any()) } returns editor

        repository = UserPreferencesRepositoryImpl(sharedPreferences)

        val updatedRates = com.ontheroad.core.model.DirectPricingRates(
            baseFareAmountCents = 12_000_00L,
            ratePerKmAmountCents = 4_000_00L,
            minimumFareAmountCents = 20_000_00L,
            includedBaseDistanceKm = 1.5,
            roadDetourMultiplier = 1.3
        )

        repository.getDirectPricingRates().test {
            awaitItem() // default item

            repository.setDirectPricingRates(updatedRates)
            val received = awaitItem()
            assertEquals(12_000_00L, received.baseFareAmountCents)
            assertEquals(4_000_00L, received.ratePerKmAmountCents)
            assertEquals(20_000_00L, received.minimumFareAmountCents)
            assertEquals(1.5, received.includedBaseDistanceKm, 0.01)
            assertEquals(1.3, received.roadDetourMultiplier, 0.01)

            cancelAndIgnoreRemainingEvents()
        }

        verify { editor.putLong(UserPreferencesRepositoryImpl.KEY_BASE_FARE_CENTS, 12_000_00L) }
        verify { editor.putLong(UserPreferencesRepositoryImpl.KEY_RATE_PER_KM_CENTS, 4_000_00L) }
        verify { editor.putLong(UserPreferencesRepositoryImpl.KEY_MIN_FARE_CENTS, 20_000_00L) }
        verify { editor.putFloat(UserPreferencesRepositoryImpl.KEY_INCLUDED_BASE_KM, 1.5f) }
        verify { editor.putFloat(UserPreferencesRepositoryImpl.KEY_ROAD_DETOUR_MULTIPLIER, 1.3f) }
    }

    @Test
    fun `rejects invalid rates without writing preferences`() = runTest {
        repository = UserPreferencesRepositoryImpl(sharedPreferences)

        var rejected = false
        try {
            repository.setDirectPricingRates(DirectPricingRates(baseFareAmountCents = -1L))
        } catch (_: IllegalArgumentException) {
            rejected = true
        }

        assertTrue(rejected)
        verify(exactly = 0) { editor.putLong(any(), any()) }
        verify(exactly = 0) { editor.putFloat(any(), any()) }
    }

    @Test
    fun `replaces invalid saved rates with defaults`() = runTest {
        every { sharedPreferences.getLong(UserPreferencesRepositoryImpl.KEY_BASE_FARE_CENTS, any()) } returns -1L
        every { sharedPreferences.getLong(UserPreferencesRepositoryImpl.KEY_RATE_PER_KM_CENTS, any()) } returns 8_000_00L
        every { sharedPreferences.getLong(UserPreferencesRepositoryImpl.KEY_MIN_FARE_CENTS, any()) } returns 20_000_00L
        every { sharedPreferences.getFloat(UserPreferencesRepositoryImpl.KEY_INCLUDED_BASE_KM, any()) } returns Float.NaN
        every { sharedPreferences.getFloat(UserPreferencesRepositoryImpl.KEY_ROAD_DETOUR_MULTIPLIER, any()) } returns 1.25f

        repository = UserPreferencesRepositoryImpl(sharedPreferences)

        repository.getDirectPricingRates().test {
            assertEquals(
                DirectPricingRates(
                    ratePerKmAmountCents = 8_000_00L,
                    minimumFareAmountCents = 20_000_00L
                ),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
