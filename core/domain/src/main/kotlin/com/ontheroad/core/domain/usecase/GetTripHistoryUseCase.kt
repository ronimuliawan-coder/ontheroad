package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class TripSortOrder {
    RECENT_FIRST,
    PROFITABILITY_HIGH_TO_LOW,
    DISTANCE_HIGH_TO_LOW
}

/**
 * UseCase to retrieve, filter, and sort trip history.
 * Pure Kotlin, zero Android dependencies (ARCH-001).
 */
class GetTripHistoryUseCase(
    private val tripRepository: TripRepository
) {

    operator fun invoke(
        sortOrder: TripSortOrder = TripSortOrder.RECENT_FIRST,
        platformIdFilter: String? = null,
        categoryIdFilter: String? = null
    ): Flow<List<Trip>> {
        return tripRepository.getAllTrips().map { trips ->
            var filtered = trips

            if (platformIdFilter != null) {
                filtered = filtered.filter { it.platformId == platformIdFilter }
            }

            if (categoryIdFilter != null) {
                filtered = filtered.filter { it.categoryId == categoryIdFilter }
            }

            when (sortOrder) {
                TripSortOrder.RECENT_FIRST -> filtered.sortedByDescending { it.startTimeMillis }
                TripSortOrder.PROFITABILITY_HIGH_TO_LOW -> filtered.sortedByDescending { trip ->
                    if (trip.actualDistanceMeters > 0) {
                        trip.totalEarningsCents / (trip.actualDistanceMeters / 1000.0)
                    } else {
                        0.0
                    }
                }
                TripSortOrder.DISTANCE_HIGH_TO_LOW -> filtered.sortedByDescending { it.actualDistanceMeters }
            }
        }
    }
}
