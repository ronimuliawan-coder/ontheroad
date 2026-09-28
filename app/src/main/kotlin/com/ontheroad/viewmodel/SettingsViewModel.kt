package com.ontheroad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontheroad.core.domain.repository.UserPreferencesRepository
import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.model.DirectPricingProfile
import com.ontheroad.core.model.DirectPricingProfileSettings
import com.ontheroad.core.model.ThemeMode
import com.ontheroad.core.model.isPersistable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = userPreferencesRepository
        .getThemeMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    val directPricingRates: StateFlow<DirectPricingRates> = userPreferencesRepository
        .getDirectPricingRates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DirectPricingRates()
        )

    val directPricingProfiles: StateFlow<DirectPricingProfileSettings> = userPreferencesRepository
        .getDirectPricingProfileSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DirectPricingProfileSettings()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun updateDirectPricingRates(rates: DirectPricingRates) {
        if (!rates.isPersistable()) return
        viewModelScope.launch {
            userPreferencesRepository.setDirectPricingRates(rates)
        }
    }

    fun updateBaseFare(baseFareCents: Long) {
        val current = directPricingProfiles.value.activeProfile.rates
        updateDirectPricingRates(current.copy(baseFareAmountCents = baseFareCents))
    }

    fun updateRatePerKm(ratePerKmCents: Long) {
        val current = directPricingProfiles.value.activeProfile.rates
        updateDirectPricingRates(current.copy(ratePerKmAmountCents = ratePerKmCents))
    }

    fun updateMinimumFare(minFareCents: Long) {
        val current = directPricingProfiles.value.activeProfile.rates
        updateDirectPricingRates(current.copy(minimumFareAmountCents = minFareCents))
    }

    fun updateIncludedBaseDistance(km: Double) {
        val current = directPricingProfiles.value.activeProfile.rates
        updateDirectPricingRates(current.copy(includedBaseDistanceKm = km))
    }

    fun selectDirectPricingProfile(profileId: String) {
        viewModelScope.launch {
            userPreferencesRepository.setActiveDirectPricingProfile(profileId)
        }
    }

    fun createDirectPricingProfile(name: String) {
        val normalizedName = name.trim()
        val current = directPricingProfiles.value
        if (normalizedName.isEmpty() ||
            normalizedName.length > DirectPricingProfileSettings.MAX_PROFILE_NAME_LENGTH ||
            current.profiles.any { it.name.equals(normalizedName, ignoreCase = true) }
        ) return

        val profile = DirectPricingProfile(
            id = UUID.randomUUID().toString(),
            name = normalizedName,
            rates = current.activeProfile.rates
        )
        viewModelScope.launch {
            userPreferencesRepository.saveDirectPricingProfile(profile)
        }
    }

    fun deleteDirectPricingProfile(profileId: String) {
        viewModelScope.launch {
            userPreferencesRepository.deleteDirectPricingProfile(profileId)
        }
    }
}
