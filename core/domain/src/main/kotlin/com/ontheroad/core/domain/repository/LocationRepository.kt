package com.ontheroad.core.domain.repository

import com.ontheroad.core.model.AddressSuggestion

/**
 * Pure Kotlin contract for device location acquisition and address geocoding.
 *
 * Adheres to ARCH-001: Zero Android imports in domain repository interfaces.
 */
interface LocationRepository {

    /**
     * Obtains the current device GPS position and reverse-geocodes it into a readable address.
     * Returns null when permission, the device provider, or address lookup is unavailable.
     */
    suspend fun getCurrentLocation(): AddressSuggestion?

    /**
     * Best-effort search for address suggestions matching [query]. Results may be empty when the
     * device geocoder is unavailable or cannot resolve the query.
     */
    suspend fun searchAddresses(
        query: String,
        biasLatitude: Double? = null,
        biasLongitude: Double? = null,
        maxResults: Int = 5
    ): List<AddressSuggestion>

    /**
     * Reverse geocodes the given coordinates into a readable address string.
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String?
}
