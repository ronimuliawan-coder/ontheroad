package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.LocationRepository
import com.ontheroad.core.model.AddressSuggestion

/**
 * Pure Kotlin UseCase to retrieve current GPS location and reverse-geocoded address.
 *
 * Adheres to ARCH-001.
 */
class GetCurrentLocationUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): AddressSuggestion? {
        return locationRepository.getCurrentLocation()
    }
}
