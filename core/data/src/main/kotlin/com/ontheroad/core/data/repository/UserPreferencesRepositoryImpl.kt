package com.ontheroad.core.data.repository

import android.content.SharedPreferences
import com.ontheroad.core.domain.repository.UserPreferencesRepository
import com.ontheroad.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : UserPreferencesRepository {

    private val themeModeState: MutableStateFlow<ThemeMode>

    init {
        val savedModeString = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        val initialMode = try {
            ThemeMode.valueOf(savedModeString ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        themeModeState = MutableStateFlow(initialMode)
    }

    override fun getThemeMode(): Flow<ThemeMode> = themeModeState.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
        themeModeState.value = mode
    }

    companion object {
        const val PREFS_NAME = "ontheroad_preferences"
        const val KEY_THEME_MODE = "key_theme_mode"
    }
}
