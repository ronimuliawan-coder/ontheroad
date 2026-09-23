package com.ontheroad.core.data.repository

import android.content.SharedPreferences
import app.cash.turbine.test
import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.model.DirectPricingProfile
import com.ontheroad.core.model.ThemeMode
import io.mockk.every
import io.mockk.firstArg
import io.mockk.mockk
import io.mockk.secondArg
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserPreferencesRepositoryTest {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var repository: UserPreferencesRepositoryImpl
    private val storedValues = mutableMapOf<String, Any?>()

    @Before
    fun setUp() {
        storedValues.clear()
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)
        every { sharedPreferences.edit() } returns editor
        every { sharedPreferences.getString(any(), any()) } answers {
            storedValues[firstArg<String>()] as? String ?: secondArg<String?>()
        }
        every { sharedPreferences.getLong(any(), any()) } answers {
            (storedValues[firstArg<String>()] as? Number)?.toLong() ?: secondArg()
        }
        every { sharedPreferences.getFloat(any(), any()) } answers {
            (storedValues[firstArg<String>()] as? Number)?.toFloat() ?: secondArg()
        }
        every { sharedPreferences.contains(any()) } answers { storedValues.containsKey(firstArg<String>()) }
        every { editor.putString(any(), any()) } answers {
            storedValues[firstArg<String>()] = secondArg<String?>()
            editor
        }
        every { editor.putLong(any(), any()) } answers {
            storedValues[firstArg<String>()] = secondArg<Long>()
            editor
        }
        every { editor.putFloat(any(), any()) } answers {
            storedValues[firstArg<String>()] = secondArg<Float>()
            editor
        }
        every { editor.remove(any()) } answers {
            storedValues.remove(firstArg<String>())
            editor
        }
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
        assertEquals("default", repository.getDirectPricingProfileSettings().first().activeProfileId)
        assertEquals("Default", repository.getDirectPricingProfileSettings().first().activeProfile.name)
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
        verify(exactly = 0) { editor.putLong(UserPreferencesRepositoryImpl.KEY_BASE_FARE_CENTS, any()) }
        verify(exactly = 0) { editor.putFloat(UserPreferencesRepositoryImpl.KEY_INCLUDED_BASE_KM, any()) }
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

    @Test
    fun `saved profile switches active rates and restores after repository recreation`() = runTest {
        repository = UserPreferencesRepositoryImpl(sharedPreferences)
        val motorcycleRates = DirectPricingRates(
            baseFareAmountCents = 20_000L,
            ratePerKmAmountCents = 3_000L,
            minimumFareAmountCents = 5_000L,
            includedBaseDistanceKm = 1.0,
            roadDetourMultiplier = 1.8
        )
        repository.saveDirectPricingProfile(
            DirectPricingProfile("motorcycle", "Motorcycle / Courier", motorcycleRates)
        )
        repository.setActiveDirectPricingProfile("motorcycle")
        val editedMotorcycleRates = motorcycleRates.copy(roadDetourMultiplier = 2.0)
        repository.setDirectPricingRates(editedMotorcycleRates)

        assertEquals(editedMotorcycleRates, repository.getDirectPricingRates().first())
        assertEquals(20_000L, sharedPreferences.getLong(UserPreferencesRepositoryImpl.KEY_BASE_FARE_CENTS, 0L))

        val restoredRepository = UserPreferencesRepositoryImpl(sharedPreferences)
        val restored = restoredRepository.getDirectPricingProfileSettings().first()
        assertEquals("motorcycle", restored.activeProfileId)
        assertEquals(editedMotorcycleRates, restored.activeProfile.rates)
        assertEquals(2.0, restored.activeProfile.rates.roadDetourMultiplier, 0.001)
    }

    @Test
    fun `rejects duplicate names and invalid profile rates`() = runTest {
        repository = UserPreferencesRepositoryImpl(sharedPreferences)
        repository.saveDirectPricingProfile(DirectPricingProfile("car", "Car", DirectPricingRates()))

        var duplicateRejected = false
        try {
            repository.saveDirectPricingProfile(DirectPricingProfile("suv", "car", DirectPricingRates()))
        } catch (_: IllegalArgumentException) {
            duplicateRejected = true
        }
        var invalidRatesRejected = false
        try {
            repository.saveDirectPricingProfile(
                DirectPricingProfile("invalid", "Invalid", DirectPricingRates(baseFareAmountCents = -1L))
            )
        } catch (_: IllegalArgumentException) {
            invalidRatesRejected = true
        }

        assertTrue(duplicateRejected)
        assertTrue(invalidRatesRejected)
        assertEquals(2, repository.getDirectPricingProfileSettings().first().profiles.size)
    }

    @Test
    fun `first profile migration preserves all existing scalar rates`() = runTest {
        storedValues[UserPreferencesRepositoryImpl.KEY_BASE_FARE_CENTS] = 120_000L
        storedValues[UserPreferencesRepositoryImpl.KEY_RATE_PER_KM_CENTS] = 45_000L
        storedValues[UserPreferencesRepositoryImpl.KEY_MIN_FARE_CENTS] = 150_000L
        storedValues[UserPreferencesRepositoryImpl.KEY_INCLUDED_BASE_KM] = 1.5f
        storedValues[UserPreferencesRepositoryImpl.KEY_ROAD_DETOUR_MULTIPLIER] = 1.7f

        repository = UserPreferencesRepositoryImpl(sharedPreferences)

        val profile = repository.getDirectPricingProfileSettings().first().activeProfile
        assertEquals("Default", profile.name)
        assertEquals(120_000L, profile.rates.baseFareAmountCents)
        assertEquals(45_000L, profile.rates.ratePerKmAmountCents)
        assertEquals(150_000L, profile.rates.minimumFareAmountCents)
        assertEquals(1.5, profile.rates.includedBaseDistanceKm, 0.001)
        assertEquals(1.7, profile.rates.roadDetourMultiplier, 0.001)
    }

    @Test
    fun `deleting active profile selects remaining profile and preserves one profile minimum`() = runTest {
        repository = UserPreferencesRepositoryImpl(sharedPreferences)
        repository.saveDirectPricingProfile(
            DirectPricingProfile("car", "Car", DirectPricingRates())
        )
        repository.setActiveDirectPricingProfile("car")
        repository.deleteDirectPricingProfile("car")

        val settings = repository.getDirectPricingProfileSettings().first()
        assertEquals(1, settings.profiles.size)
        assertEquals("default", settings.activeProfileId)

        var rejected = false
        try {
            repository.deleteDirectPricingProfile("default")
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }

    @Test
    fun `profile restore keeps rates edited by an older app version`() = runTest {
        repository = UserPreferencesRepositoryImpl(sharedPreferences)
        repository.saveDirectPricingProfile(
            DirectPricingProfile(
                "motorcycle",
                "Motorcycle",
                DirectPricingRates(ratePerKmAmountCents = 3_000L)
            )
        )
        repository.setActiveDirectPricingProfile("motorcycle")
        storedValues[UserPreferencesRepositoryImpl.KEY_RATE_PER_KM_CENTS] = 4_500L

        val restoredRepository = UserPreferencesRepositoryImpl(sharedPreferences)
        val settings = restoredRepository.getDirectPricingProfileSettings().first()

        assertEquals(4_500L, settings.activeProfile.rates.ratePerKmAmountCents)
        assertEquals(3_500_00L, settings.profiles.first().rates.ratePerKmAmountCents)
    }
}
