package com.ontheroad.core.data.repository

import android.content.SharedPreferences
import com.ontheroad.core.domain.repository.UserPreferencesRepository
import com.ontheroad.core.model.DirectPricingProfile
import com.ontheroad.core.model.DirectPricingProfileSettings
import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.model.ThemeMode
import com.ontheroad.core.model.isPersistable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Locale

class UserPreferencesRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : UserPreferencesRepository {

    private val themeModeState: MutableStateFlow<ThemeMode>
    private val directPricingProfilesState: MutableStateFlow<DirectPricingProfileSettings>

    init {
        val savedModeString = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        val initialMode = try {
            ThemeMode.valueOf(savedModeString ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        themeModeState = MutableStateFlow(initialMode)
        directPricingProfilesState = MutableStateFlow(restoreDirectPricingProfiles())
    }

    override fun getThemeMode(): Flow<ThemeMode> = themeModeState.asStateFlow()

    override suspend fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
        themeModeState.value = mode
    }

    override fun getDirectPricingRates(): Flow<DirectPricingRates> =
        directPricingProfilesState
            .map { it.activeProfile.rates }
            .distinctUntilChanged()

    override suspend fun setDirectPricingRates(rates: DirectPricingRates) {
        require(rates.isPersistable()) { "Direct pricing rates must be non-negative and persistable" }
        val current = directPricingProfilesState.value
        val updatedProfiles = current.profiles.map { profile ->
            if (profile.id == current.activeProfileId) profile.copy(rates = rates) else profile
        }
        persistDirectPricingProfiles(current.copy(profiles = updatedProfiles))
    }

    override fun getDirectPricingProfileSettings(): Flow<DirectPricingProfileSettings> =
        directPricingProfilesState.asStateFlow()

    override suspend fun setActiveDirectPricingProfile(profileId: String) {
        val current = directPricingProfilesState.value
        require(current.profiles.any { it.id == profileId }) { "Unknown direct pricing profile: $profileId" }
        persistDirectPricingProfiles(current.copy(activeProfileId = profileId))
    }

    override suspend fun saveDirectPricingProfile(profile: DirectPricingProfile) {
        val normalized = profile.copy(name = profile.name.trim())
        require(normalized.isPersistable()) { "Direct pricing profile must have a valid name and rates" }

        val current = directPricingProfilesState.value
        require(current.profiles.none {
            it.id != normalized.id && it.name.equals(normalized.name, ignoreCase = true)
        }) { "Direct pricing profile names must be unique" }

        val existingIndex = current.profiles.indexOfFirst { it.id == normalized.id }
        val updatedProfiles = if (existingIndex >= 0) {
            current.profiles.toMutableList().also { it[existingIndex] = normalized }
        } else {
            current.profiles + normalized
        }
        persistDirectPricingProfiles(current.copy(profiles = updatedProfiles))
    }

    override suspend fun deleteDirectPricingProfile(profileId: String) {
        val current = directPricingProfilesState.value
        require(current.profiles.size > 1) { "At least one direct pricing profile must remain" }
        require(current.profiles.any { it.id == profileId }) { "Unknown direct pricing profile: $profileId" }

        val remainingProfiles = current.profiles.filterNot { it.id == profileId }
        val activeProfileId = if (current.activeProfileId == profileId) {
            remainingProfiles.first().id
        } else {
            current.activeProfileId
        }
        persistDirectPricingProfiles(
            DirectPricingProfileSettings(
                profiles = remainingProfiles,
                activeProfileId = activeProfileId
            )
        )
    }

    private fun restoreDirectPricingProfiles(): DirectPricingProfileSettings {
        val storedIds = sharedPreferences.getString(KEY_DIRECT_PRICING_PROFILE_IDS, null)
        if (storedIds == null) {
            val legacyRates = readLegacyDirectPricingRates()
            val defaultProfile = DirectPricingProfile(
                id = DirectPricingProfileSettings.DEFAULT_PROFILE_ID,
                name = DirectPricingProfileSettings.DEFAULT_PROFILE_NAME,
                rates = legacyRates
            )
            val initial = DirectPricingProfileSettings(listOf(defaultProfile), defaultProfile.id)
            writeProfileSettings(initial, oldProfileIds = emptySet(), mirrorLegacyRates = false)
            return initial
        }

        val profiles = storedIds
            .split(PROFILE_ID_SEPARATOR)
            .filter(String::isNotBlank)
            .distinct()
            .mapNotNull(::readProfile)

        if (profiles.isEmpty()) {
            val legacyRates = readLegacyDirectPricingRates()
            val fallback = DirectPricingProfile(
                id = DirectPricingProfileSettings.DEFAULT_PROFILE_ID,
                name = DirectPricingProfileSettings.DEFAULT_PROFILE_NAME,
                rates = legacyRates
            )
            return DirectPricingProfileSettings(listOf(fallback), fallback.id)
        }

        val requestedActiveId = sharedPreferences.getString(KEY_ACTIVE_DIRECT_PRICING_PROFILE_ID, null)
        val activeProfileId = profiles.firstOrNull { it.id == requestedActiveId }?.id ?: profiles.first().id
        val settings = DirectPricingProfileSettings(profiles, activeProfileId)
        val legacyRates = readCompleteLegacyDirectPricingRates()
        if (legacyRates != null && legacyRates != settings.activeProfile.rates) {
            val reconciled = settings.copy(
                profiles = settings.profiles.map { profile ->
                    if (profile.id == activeProfileId) profile.copy(rates = legacyRates) else profile
                }
            )
            writeProfileSettings(reconciled, settings.profiles.mapTo(mutableSetOf()) { it.id }, false)
            return reconciled
        }
        return settings
    }

    private fun readProfile(profileId: String): DirectPricingProfile? {
        val name = sharedPreferences.getString(profileKey(profileId, SUFFIX_NAME), null)
            ?.takeIf(String::isNotBlank)
            ?: return null
        val defaults = DirectPricingRates()
        val rates = DirectPricingRates(
            baseFareAmountCents = sharedPreferences.getLong(
                profileKey(profileId, SUFFIX_BASE_FARE_CENTS), defaults.baseFareAmountCents
            ).takeIf { it >= 0L } ?: defaults.baseFareAmountCents,
            ratePerKmAmountCents = sharedPreferences.getLong(
                profileKey(profileId, SUFFIX_RATE_PER_KM_CENTS), defaults.ratePerKmAmountCents
            ).takeIf { it >= 0L } ?: defaults.ratePerKmAmountCents,
            minimumFareAmountCents = sharedPreferences.getLong(
                profileKey(profileId, SUFFIX_MIN_FARE_CENTS), defaults.minimumFareAmountCents
            ).takeIf { it >= 0L } ?: defaults.minimumFareAmountCents,
            includedBaseDistanceKm = sharedPreferences.getFloat(
                profileKey(profileId, SUFFIX_INCLUDED_BASE_KM), defaults.includedBaseDistanceKm.toFloat()
            ).toDouble().takeIf { it.isFinite() && it >= 0.0 } ?: defaults.includedBaseDistanceKm,
            roadDetourMultiplier = sharedPreferences.getFloat(
                profileKey(profileId, SUFFIX_ROAD_DETOUR_MULTIPLIER), defaults.roadDetourMultiplier.toFloat()
            ).toDouble().takeIf { it.isFinite() && it >= 0.0 } ?: defaults.roadDetourMultiplier
        )
        return DirectPricingProfile(profileId, name, rates)
    }

    private fun readLegacyDirectPricingRates(): DirectPricingRates {
        val defaults = DirectPricingRates()
        return DirectPricingRates(
            baseFareAmountCents = sharedPreferences.getLong(
                KEY_BASE_FARE_CENTS, DEFAULT_BASE_FARE_CENTS
            ).takeIf { it >= 0L } ?: defaults.baseFareAmountCents,
            ratePerKmAmountCents = sharedPreferences.getLong(
                KEY_RATE_PER_KM_CENTS, DEFAULT_RATE_PER_KM_CENTS
            ).takeIf { it >= 0L } ?: defaults.ratePerKmAmountCents,
            minimumFareAmountCents = sharedPreferences.getLong(
                KEY_MIN_FARE_CENTS, DEFAULT_MIN_FARE_CENTS
            ).takeIf { it >= 0L } ?: defaults.minimumFareAmountCents,
            includedBaseDistanceKm = sharedPreferences.getFloat(
                KEY_INCLUDED_BASE_KM, DEFAULT_INCLUDED_BASE_KM.toFloat()
            ).toDouble().takeIf { it.isFinite() && it >= 0.0 } ?: defaults.includedBaseDistanceKm,
            roadDetourMultiplier = sharedPreferences.getFloat(
                KEY_ROAD_DETOUR_MULTIPLIER, DEFAULT_ROAD_DETOUR_MULTIPLIER.toFloat()
            ).toDouble().takeIf { it.isFinite() && it >= 0.0 } ?: defaults.roadDetourMultiplier
        )
    }

    private fun readCompleteLegacyDirectPricingRates(): DirectPricingRates? {
        val legacyKeys = listOf(
            KEY_BASE_FARE_CENTS,
            KEY_RATE_PER_KM_CENTS,
            KEY_MIN_FARE_CENTS,
            KEY_INCLUDED_BASE_KM,
            KEY_ROAD_DETOUR_MULTIPLIER
        )
        if (!legacyKeys.all(sharedPreferences::contains)) return null

        return runCatching {
            DirectPricingRates(
                baseFareAmountCents = sharedPreferences.getLong(KEY_BASE_FARE_CENTS, -1L),
                ratePerKmAmountCents = sharedPreferences.getLong(KEY_RATE_PER_KM_CENTS, -1L),
                minimumFareAmountCents = sharedPreferences.getLong(KEY_MIN_FARE_CENTS, -1L),
                includedBaseDistanceKm = sharedPreferences.getFloat(KEY_INCLUDED_BASE_KM, Float.NaN).toDouble(),
                roadDetourMultiplier = sharedPreferences.getFloat(
                    KEY_ROAD_DETOUR_MULTIPLIER,
                    Float.NaN
                ).toDouble()
            ).takeIf(DirectPricingRates::isPersistable)
        }.getOrNull()
    }

    private fun persistDirectPricingProfiles(settings: DirectPricingProfileSettings) {
        require(settings.profiles.isNotEmpty()) { "At least one direct pricing profile must remain" }
        require(settings.profiles.map { it.id }.distinct().size == settings.profiles.size) {
            "Direct pricing profile IDs must be unique"
        }
        require(settings.profiles.any { it.id == settings.activeProfileId }) {
            "The active direct pricing profile must exist"
        }
        require(settings.profiles.all(DirectPricingProfile::isPersistable)) {
            "Direct pricing profiles must have valid names and rates"
        }
        require(settings.profiles.map { it.name.lowercase(Locale.ROOT) }.distinct().size == settings.profiles.size) {
            "Direct pricing profile names must be unique"
        }

        writeProfileSettings(
            settings = settings,
            oldProfileIds = directPricingProfilesState.value.profiles.mapTo(mutableSetOf()) { it.id },
            mirrorLegacyRates = true
        )
        directPricingProfilesState.value = settings
    }

    private fun writeProfileSettings(
        settings: DirectPricingProfileSettings,
        oldProfileIds: Set<String>,
        mirrorLegacyRates: Boolean
    ) {
        val editor = sharedPreferences.edit()
        val newProfileIds = settings.profiles.mapTo(mutableSetOf()) { it.id }
        (oldProfileIds - newProfileIds).forEach { profileId ->
            PROFILE_SUFFIXES.forEach { suffix -> editor.remove(profileKey(profileId, suffix)) }
        }
        settings.profiles.forEach { profile ->
            editor.putString(profileKey(profile.id, SUFFIX_NAME), profile.name)
            editor.putLong(profileKey(profile.id, SUFFIX_BASE_FARE_CENTS), profile.rates.baseFareAmountCents)
            editor.putLong(profileKey(profile.id, SUFFIX_RATE_PER_KM_CENTS), profile.rates.ratePerKmAmountCents)
            editor.putLong(profileKey(profile.id, SUFFIX_MIN_FARE_CENTS), profile.rates.minimumFareAmountCents)
            editor.putFloat(profileKey(profile.id, SUFFIX_INCLUDED_BASE_KM), profile.rates.includedBaseDistanceKm.toFloat())
            editor.putFloat(profileKey(profile.id, SUFFIX_ROAD_DETOUR_MULTIPLIER), profile.rates.roadDetourMultiplier.toFloat())
        }
        editor.putString(KEY_DIRECT_PRICING_PROFILE_IDS, settings.profiles.joinToString(PROFILE_ID_SEPARATOR) { it.id })
        editor.putString(KEY_ACTIVE_DIRECT_PRICING_PROFILE_ID, settings.activeProfileId)
        if (mirrorLegacyRates) writeLegacyRates(editor, settings.activeProfile.rates)
        editor.apply()
    }

    private fun writeLegacyRates(editor: SharedPreferences.Editor, rates: DirectPricingRates) {
        editor.putLong(KEY_BASE_FARE_CENTS, rates.baseFareAmountCents)
        editor.putLong(KEY_RATE_PER_KM_CENTS, rates.ratePerKmAmountCents)
        editor.putLong(KEY_MIN_FARE_CENTS, rates.minimumFareAmountCents)
        editor.putFloat(KEY_INCLUDED_BASE_KM, rates.includedBaseDistanceKm.toFloat())
        editor.putFloat(KEY_ROAD_DETOUR_MULTIPLIER, rates.roadDetourMultiplier.toFloat())
    }

    private fun profileKey(profileId: String, suffix: String): String =
        "$KEY_DIRECT_PRICING_PROFILE_PREFIX$profileId$suffix"

    companion object {
        const val PREFS_NAME = "ontheroad_preferences"
        const val KEY_THEME_MODE = "key_theme_mode"

        const val KEY_BASE_FARE_CENTS = "key_base_fare_cents"
        const val KEY_RATE_PER_KM_CENTS = "key_rate_per_km_cents"
        const val KEY_MIN_FARE_CENTS = "key_min_fare_cents"
        const val KEY_INCLUDED_BASE_KM = "key_included_base_km"
        const val KEY_ROAD_DETOUR_MULTIPLIER = "key_road_detour_multiplier"

        const val KEY_DIRECT_PRICING_PROFILE_IDS = "key_direct_pricing_profile_ids"
        const val KEY_ACTIVE_DIRECT_PRICING_PROFILE_ID = "key_active_direct_pricing_profile_id"
        const val KEY_DIRECT_PRICING_PROFILE_PREFIX = "key_direct_pricing_profile_"

        const val DEFAULT_BASE_FARE_CENTS = 10_000_00L
        const val DEFAULT_RATE_PER_KM_CENTS = 3_500_00L
        const val DEFAULT_MIN_FARE_CENTS = 15_000_00L
        const val DEFAULT_INCLUDED_BASE_KM = 0.0
        const val DEFAULT_ROAD_DETOUR_MULTIPLIER = 1.25

        private const val PROFILE_ID_SEPARATOR = "\n"
        private const val SUFFIX_NAME = "_name"
        private const val SUFFIX_BASE_FARE_CENTS = "_base_fare_cents"
        private const val SUFFIX_RATE_PER_KM_CENTS = "_rate_per_km_cents"
        private const val SUFFIX_MIN_FARE_CENTS = "_min_fare_cents"
        private const val SUFFIX_INCLUDED_BASE_KM = "_included_base_km"
        private const val SUFFIX_ROAD_DETOUR_MULTIPLIER = "_road_detour_multiplier"
        private val PROFILE_SUFFIXES = listOf(
            SUFFIX_NAME,
            SUFFIX_BASE_FARE_CENTS,
            SUFFIX_RATE_PER_KM_CENTS,
            SUFFIX_MIN_FARE_CENTS,
            SUFFIX_INCLUDED_BASE_KM,
            SUFFIX_ROAD_DETOUR_MULTIPLIER
        )
    }
}
