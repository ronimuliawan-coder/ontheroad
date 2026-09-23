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
     */
    suspend fun getCurrentLocation(): AddressSuggestion?

    /**
     * Searches for address suggestions matching [query], optionally biased around [biasLatitude], [biasLongitude].
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
