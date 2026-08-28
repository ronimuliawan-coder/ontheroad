package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.FakeTripRepository
import com.ontheroad.core.model.TripStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CompleteTripUseCaseTest {

    private lateinit var tripRepository: FakeTripRepository
    private lateinit var startTripUseCase: StartTripUseCase
    private lateinit var completeTripUseCase: CompleteTripUseCase

    @Before
    fun setUp() {
        tripRepository = FakeTripRepository()
        startTripUseCase = StartTripUseCase(tripRepository)
        completeTripUseCase = CompleteTripUseCase(tripRepository)
    }

    @Test
    fun completesTripAndFinalizesMetrics() = runTest {
        val startResult = startTripUseCase(
            startAddress = "Monas, Jakarta",
            startLatitude = -6.175392,
            startLongitude = 106.827153,
            platformId = "grab",
            categoryId = "passenger",
            startTimeMillis = 1000000
        )
        val trip = startResult.getOrThrow()

        // Complete trip 30 minutes later (1800 seconds)
        val completeResult = completeTripUseCase(
            tripId = trip.id,
            endAddress = "Bundaran HI, Jakarta",
            endLatitude = -6.195000,
            endLongitude = 106.823056,
            platformFeeAmountCents = 25000,
            cashCollectedAmountCents = 10000,
            tipAmountCents = 5000,
            quotedDistanceMeters = 4200.0,
            notes = "Smooth passenger run",
            endTimeMillis = 1000000 + 1800000
        )

        assertTrue(completeResult.isSuccess)
        val completedTrip = completeResult.getOrThrow()

        assertEquals(TripStatus.COMPLETED, completedTrip.status)
        assertEquals("Bundaran HI, Jakarta", completedTrip.endAddress)
        assertEquals(1800L, completedTrip.durationSeconds)
        assertEquals(25000L, completedTrip.platformFeeAmountCents)
        assertEquals(10000L, completedTrip.cashCollectedAmountCents)
        assertEquals(5000L, completedTrip.tipAmountCents)
        assertEquals(40000L, completedTrip.totalEarningsCents)
        assertEquals(4200.0, completedTrip.quotedDistanceMeters!!, 0.01)

        val stored = tripRepository.getTripById(trip.id)
        assertNotNull(stored)
        assertEquals(TripStatus.COMPLETED, stored?.status)
    }
}
