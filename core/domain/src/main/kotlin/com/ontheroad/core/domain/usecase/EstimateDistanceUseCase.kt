package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.geo.HaversineDistanceCalculator

/**
 * UseCase to estimate road distance between two geographical points using
 * Haversine spherical distance and a road detour multiplier.
 * Pure Kotlin, zero Android framework dependencies (ARCH-001).
 */
class EstimateDistanceUseCase {

    /**
     * Calculates estimated road distance in meters.
     *
     * @param startLat Pickup latitude.
     * @param startLng Pickup longitude.
     * @param endLat Destination latitude.
     * @param endLng Destination longitude.
     * @param roadDetourMultiplier Detour factor to account for road curvature (default 1.25).
     * @return Estimated road distance in meters.
     */
    operator fun invoke(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        roadDetourMultiplier: Double = 1.25
    ): Double {
        val straightLineDistanceMeters = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = startLat,
            startLng = startLng,
            endLat = endLat,
            endLng = endLng
        )
        return straightLineDistanceMeters * maxOf(1.0, roadDetourMultiplier)
    }
}
