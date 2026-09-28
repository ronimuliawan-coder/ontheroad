package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.LocationRepository
import com.ontheroad.core.model.AddressSuggestion

/**
 * Pure Kotlin UseCase to search for geocoded address suggestions.
 *
 * Adheres to ARCH-001.
 */
class SearchAddressUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(
        query: String,
        biasLatitude: Double? = null,
        biasLongitude: Double? = null,
        maxResults: Int = 5
    ): List<AddressSuggestion> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        return locationRepository.searchAddresses(
            query = trimmed,
            biasLatitude = biasLatitude,
            biasLongitude = biasLongitude,
            maxResults = maxResults
        )
    }
}
