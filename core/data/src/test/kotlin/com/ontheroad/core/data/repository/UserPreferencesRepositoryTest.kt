package com.ontheroad.core.data.repository

import android.content.SharedPreferences
import app.cash.turbine.test
import com.ontheroad.core.model.ThemeMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
}
