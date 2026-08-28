package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.geo.GpsJitterFilter
import com.ontheroad.core.domain.repository.FakeTripRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecordRoutePointUseCaseTest {

    private lateinit var tripRepository: FakeTripRepository
    private lateinit var startTripUseCase: StartTripUseCase
    private lateinit var recordRoutePointUseCase: RecordRoutePointUseCase

    @Before
    fun setUp() {
        tripRepository = FakeTripRepository()
        startTripUseCase = StartTripUseCase(tripRepository)
        recordRoutePointUseCase = RecordRoutePointUseCase(
            tripRepository = tripRepository,
            jitterFilter = GpsJitterFilter()
        )
    }

    @Test
    fun accumulatesOdometerForValidVehicleMovement() = runTest {
        val startResult = startTripUseCase(
            startAddress = "Monas, Jakarta",
            startLatitude = -6.175392,
            startLongitude = 106.827153,
            platformId = "grab",
            categoryId = "passenger",
            startTimeMillis = 1000
        )
        val trip = startResult.getOrThrow()

        // Move ~110 meters south in 10 seconds (11 m/s = ~40 km/h)
        val updatedTrip = recordRoutePointUseCase(
            tripId = trip.id,
            latitude = -6.176392,
            longitude = 106.827153,
            accuracyMeters = 5f,
            speedMps = 11f,
            timestampMillis = 11000
        )

        assertNotNull(updatedTrip)
        updatedTrip!!

        // Expect ~111 meters added
        assertTrue(updatedTrip.actualDistanceMeters > 100.0)
        assertTrue(updatedTrip.actualDistanceMeters < 120.0)

        // Verify points were recorded
        val points = tripRepository.getRoutePointsForTrip(trip.id).first()
        assertEquals(2, points.size)
    }

    @Test
    fun rejectsStationaryJitterWithoutAccumulatingDistance() = runTest {
        val startResult = startTripUseCase(
            startAddress = "Monas, Jakarta",
            startLatitude = -6.175392,
            startLongitude = 106.827153,
            platformId = "grab",
            categoryId = "passenger",
            startTimeMillis = 1000
        )
        val trip = startResult.getOrThrow()

        // Tiny drift of 1 meter while stopped at red light (speed 0.1 m/s)
        val updatedTrip = recordRoutePointUseCase(
            tripId = trip.id,
            latitude = -6.175401,
            longitude = 106.827153,
            accuracyMeters = 8f,
            speedMps = 0.1f,
            timestampMillis = 2000
        )

        assertNotNull(updatedTrip)
        // Distance should remain 0.0 because point was rejected as jitter
        assertEquals(0.0, updatedTrip!!.actualDistanceMeters, 0.001)

        val points = tripRepository.getRoutePointsForTrip(trip.id).first()
        assertEquals(1, points.size) // Only initial start point remains
    }
}
