package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.model.RoutePoint
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus

/**
 * UseCase to finalize and complete an in-progress trip.
 * Pure Kotlin, zero Android dependencies (ARCH-001).
 */
class CompleteTripUseCase(
    private val tripRepository: TripRepository
) {

    suspend operator fun invoke(
        tripId: String,
        endAddress: String,
        endLatitude: Double,
        endLongitude: Double,
        platformFeeAmountCents: Long = 0,
        cashCollectedAmountCents: Long = 0,
        tipAmountCents: Long = 0,
        quotedDistanceMeters: Double? = null,
        notes: String = "",
        endTimeMillis: Long = System.currentTimeMillis()
    ): Result<Trip> {
        val trip = tripRepository.getTripById(tripId)
            ?: return Result.failure(IllegalArgumentException("Trip with ID '$tripId' not found"))

        if (trip.status != TripStatus.IN_PROGRESS && trip.status != TripStatus.PAUSED) {
            return Result.failure(IllegalStateException("Trip '$tripId' is not currently in progress"))
        }

        val durationSeconds = maxOf(0L, (endTimeMillis - trip.startTimeMillis) / 1000)

        // Record final destination point
        val finalPoint = RoutePoint(
            tripId = tripId,
            latitude = endLatitude,
            longitude = endLongitude,
            timestampMillis = endTimeMillis
        )
        tripRepository.insertRoutePoint(finalPoint)

        val completedTrip = trip.copy(
            endAddress = endAddress,
            endLatitude = endLatitude,
            endLongitude = endLongitude,
            endTimeMillis = endTimeMillis,
            durationSeconds = durationSeconds,
            platformFeeAmountCents = platformFeeAmountCents,
            cashCollectedAmountCents = cashCollectedAmountCents,
            tipAmountCents = tipAmountCents,
            quotedDistanceMeters = quotedDistanceMeters,
            notes = notes,
            status = TripStatus.COMPLETED
        )

        tripRepository.updateTrip(completedTrip)

        return Result.success(completedTrip)
    }
}
