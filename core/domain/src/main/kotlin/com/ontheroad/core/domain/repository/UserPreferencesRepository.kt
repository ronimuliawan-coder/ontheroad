package com.ontheroad.core.domain.repository

import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.model.DirectPricingProfile
import com.ontheroad.core.model.DirectPricingProfileSettings
import com.ontheroad.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for driver app preferences.
 * Follows ARCH-001 (Pure Kotlin, zero Android framework dependencies).
 */
interface UserPreferencesRepository {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    fun getDirectPricingRates(): Flow<DirectPricingRates>
    suspend fun setDirectPricingRates(rates: DirectPricingRates)
    fun getDirectPricingProfileSettings(): Flow<DirectPricingProfileSettings>
    suspend fun setActiveDirectPricingProfile(profileId: String)
    suspend fun saveDirectPricingProfile(profile: DirectPricingProfile)
    suspend fun deleteDirectPricingProfile(profileId: String)
}
