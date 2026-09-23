package com.ontheroad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontheroad.core.domain.repository.UserPreferencesRepository
import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.model.ThemeMode
import com.ontheroad.core.model.isPersistable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
        val current = directPricingRates.value
        updateDirectPricingRates(current.copy(baseFareAmountCents = baseFareCents))
    }

    fun updateRatePerKm(ratePerKmCents: Long) {
        val current = directPricingRates.value
        updateDirectPricingRates(current.copy(ratePerKmAmountCents = ratePerKmCents))
    }

    fun updateMinimumFare(minFareCents: Long) {
        val current = directPricingRates.value
        updateDirectPricingRates(current.copy(minimumFareAmountCents = minFareCents))
    }

    fun updateIncludedBaseDistance(km: Double) {
        val current = directPricingRates.value
        updateDirectPricingRates(current.copy(includedBaseDistanceKm = km))
    }
}
