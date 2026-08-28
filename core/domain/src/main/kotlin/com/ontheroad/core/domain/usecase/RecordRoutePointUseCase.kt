package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.geo.GpsJitterFilter
import com.ontheroad.core.domain.geo.HaversineDistanceCalculator
import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.model.RoutePoint
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus
import kotlinx.coroutines.flow.firstOrNull

/**
 * UseCase to evaluate and record incoming GPS location points during an active trip.
 * Filters stationary noise and accumulates real-world odometer distance.
 * Pure Kotlin, zero Android dependencies (ARCH-001).
 */
class RecordRoutePointUseCase(
    private val tripRepository: TripRepository,
    private val jitterFilter: GpsJitterFilter = GpsJitterFilter()
) {

    suspend operator fun invoke(
        tripId: String,
        latitude: Double,
        longitude: Double,
        altitude: Double = 0.0,
        accuracyMeters: Float = 0f,
        speedMps: Float = 0f,
        timestampMillis: Long = System.currentTimeMillis()
    ): Trip? {
        val trip = tripRepository.getTripById(tripId) ?: return null
        if (trip.status != TripStatus.IN_PROGRESS) return null

        val previousPoints = tripRepository.getRoutePointsForTrip(tripId).firstOrNull().orEmpty()
        val lastPoint = previousPoints.lastOrNull()

        val candidatePoint = RoutePoint(
            tripId = tripId,
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            accuracyMeters = accuracyMeters,
            speedMps = speedMps,
            timestampMillis = timestampMillis
        )

        // Evaluate jitter filter
        if (!jitterFilter.shouldAcceptPoint(lastPoint, candidatePoint)) {
            return trip // Point rejected, return existing trip unmodified
        }

        // Calculate incremental distance from previous accepted point
        val incrementalDistanceMeters = if (lastPoint != null) {
            HaversineDistanceCalculator.calculateDistanceMeters(
                startLat = lastPoint.latitude,
                startLng = lastPoint.longitude,
                endLat = candidatePoint.latitude,
                endLng = candidatePoint.longitude
            )
        } else {
            0.0
        }

        // Save accepted route point
        tripRepository.insertRoutePoint(candidatePoint)

        // Update trip cumulative distance
        val updatedTrip = trip.copy(
            actualDistanceMeters = trip.actualDistanceMeters + incrementalDistanceMeters
        )
        tripRepository.updateTrip(updatedTrip)

        return updatedTrip
    }
}
