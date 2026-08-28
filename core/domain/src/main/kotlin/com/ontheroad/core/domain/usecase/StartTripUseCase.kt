package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.model.RoutePoint
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

/**
 * UseCase to start a new trip.
 * Enforces active trip exclusivity (only 1 active trip at any time).
 * Pure Kotlin, zero Android dependencies (ARCH-001).
 */
class StartTripUseCase(
    private val tripRepository: TripRepository
) {

    suspend operator fun invoke(
        startAddress: String,
        startLatitude: Double,
        startLongitude: Double,
        platformId: String,
        categoryId: String,
        shiftId: String? = null,
        startTimeMillis: Long = System.currentTimeMillis()
    ): Result<Trip> {
        // Enforce active trip exclusivity
        val activeTrip = tripRepository.getActiveTrip().firstOrNull()
        if (activeTrip != null) {
            return Result.failure(
                IllegalStateException("Cannot start a new trip while trip '${activeTrip.id}' is still in progress")
            )
        }

        val tripId = UUID.randomUUID().toString()
        val trip = Trip(
            id = tripId,
            shiftId = shiftId,
            platformId = platformId,
            categoryId = categoryId,
            startTimeMillis = startTimeMillis,
            startAddress = startAddress,
            startLatitude = startLatitude,
            startLongitude = startLongitude,
            status = TripStatus.IN_PROGRESS
        )

        tripRepository.insertTrip(trip)

        // Record initial route point
        val initialPoint = RoutePoint(
            tripId = tripId,
            latitude = startLatitude,
            longitude = startLongitude,
            timestampMillis = startTimeMillis
        )
        tripRepository.insertRoutePoint(initialPoint)

        return Result.success(trip)
    }
}
