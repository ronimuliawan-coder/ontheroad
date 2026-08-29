package com.ontheroad.core.domain.repository

import com.ontheroad.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for driver app preferences.
 * Follows ARCH-001 (Pure Kotlin, zero Android framework dependencies).
 */
interface UserPreferencesRepository {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
