package com.ontheroad.core.data.repository

import android.content.SharedPreferences
import com.ontheroad.core.domain.repository.UserPreferencesRepository
import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.model.ThemeMode
import com.ontheroad.core.model.isPersistable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : UserPreferencesRepository {

    private val themeModeState: MutableStateFlow<ThemeMode>
    private val directPricingRatesState: MutableStateFlow<DirectPricingRates>

    init {
        val savedModeString = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        val initialMode = try {
            ThemeMode.valueOf(savedModeString ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        themeModeState = MutableStateFlow(initialMode)

        val baseFare = sharedPreferences.getLong(KEY_BASE_FARE_CENTS, DEFAULT_BASE_FARE_CENTS)
        val ratePerKm = sharedPreferences.getLong(KEY_RATE_PER_KM_CENTS, DEFAULT_RATE_PER_KM_CENTS)
        val minFare = sharedPreferences.getLong(KEY_MIN_FARE_CENTS, DEFAULT_MIN_FARE_CENTS)
        val includedBaseKm = sharedPreferences.getFloat(KEY_INCLUDED_BASE_KM, DEFAULT_INCLUDED_BASE_KM.toFloat()).toDouble()
        val detourMultiplier = sharedPreferences.getFloat(KEY_ROAD_DETOUR_MULTIPLIER, DEFAULT_ROAD_DETOUR_MULTIPLIER.toFloat()).toDouble()

        val defaultRates = DirectPricingRates()
        directPricingRatesState = MutableStateFlow(
            DirectPricingRates(
                baseFareAmountCents = baseFare.takeIf { it >= 0L } ?: defaultRates.baseFareAmountCents,
                ratePerKmAmountCents = ratePerKm.takeIf { it >= 0L } ?: defaultRates.ratePerKmAmountCents,
                minimumFareAmountCents = minFare.takeIf { it >= 0L } ?: defaultRates.minimumFareAmountCents,
                includedBaseDistanceKm = includedBaseKm.takeIf {
                    it.isFinite() && it >= 0.0 && it.toFloat().isFinite()
                } ?: defaultRates.includedBaseDistanceKm,
                roadDetourMultiplier = detourMultiplier.takeIf {
                    it.isFinite() && it >= 0.0 && it.toFloat().isFinite()
                } ?: defaultRates.roadDetourMultiplier
            )
        )
    }

    override fun getThemeMode(): Flow<ThemeMode> = themeModeState.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
        themeModeState.value = mode
    }

    override fun getDirectPricingRates(): Flow<DirectPricingRates> = directPricingRatesState.asStateFlow()

    override suspend fun setDirectPricingRates(rates: DirectPricingRates) {
        require(rates.isPersistable()) { "Direct pricing rates must be non-negative and persistable" }
        sharedPreferences.edit()
            .putLong(KEY_BASE_FARE_CENTS, rates.baseFareAmountCents)
            .putLong(KEY_RATE_PER_KM_CENTS, rates.ratePerKmAmountCents)
            .putLong(KEY_MIN_FARE_CENTS, rates.minimumFareAmountCents)
            .putFloat(KEY_INCLUDED_BASE_KM, rates.includedBaseDistanceKm.toFloat())
            .putFloat(KEY_ROAD_DETOUR_MULTIPLIER, rates.roadDetourMultiplier.toFloat())
            .apply()
        directPricingRatesState.value = rates
    }

    companion object {
        const val PREFS_NAME = "ontheroad_preferences"
        const val KEY_THEME_MODE = "key_theme_mode"

        const val KEY_BASE_FARE_CENTS = "key_base_fare_cents"
        const val KEY_RATE_PER_KM_CENTS = "key_rate_per_km_cents"
        const val KEY_MIN_FARE_CENTS = "key_min_fare_cents"
        const val KEY_INCLUDED_BASE_KM = "key_included_base_km"
        const val KEY_ROAD_DETOUR_MULTIPLIER = "key_road_detour_multiplier"

        const val DEFAULT_BASE_FARE_CENTS = 10_000_00L
        const val DEFAULT_RATE_PER_KM_CENTS = 3_500_00L
        const val DEFAULT_MIN_FARE_CENTS = 15_000_00L
        const val DEFAULT_INCLUDED_BASE_KM = 0.0
        const val DEFAULT_ROAD_DETOUR_MULTIPLIER = 1.25
    }
}
