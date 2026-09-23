package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.FakeTripRepository
import com.ontheroad.core.model.TripStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartTripUseCaseTest {

    private lateinit var tripRepository: FakeTripRepository
    private lateinit var startTripUseCase: StartTripUseCase

    @Before
    fun setUp() {
        tripRepository = FakeTripRepository()
        startTripUseCase = StartTripUseCase(tripRepository)
    }

    @Test
    fun startsTripSuccessfullyWhenNoActiveTrip() = runTest {
        val result = startTripUseCase(
            startAddress = "Monas, Jakarta",
            startLatitude = -6.175392,
            startLongitude = 106.827153,
            platformId = "grab",
            categoryId = "passenger"
        )

        assertTrue(result.isSuccess)
        val trip = result.getOrNull()
        assertNotNull(trip)
        trip!!

        assertEquals("Monas, Jakarta", trip.startAddress)
        assertEquals(TripStatus.IN_PROGRESS, trip.status)
        assertEquals(0.0, trip.actualDistanceMeters, 0.001)

        // Verify trip was persisted in repository
        val storedTrip = tripRepository.getTripById(trip.id)
        assertNotNull(storedTrip)
        assertEquals(trip.id, storedTrip?.id)
    }

    @Test
    fun preventsStartingTripWhenAnotherTripIsAlreadyActive() = runTest {
        // Start first trip
        val firstResult = startTripUseCase(
            startAddress = "Monas, Jakarta",
            startLatitude = -6.175392,
            startLongitude = 106.827153,
            platformId = "grab",
            categoryId = "passenger"
        )
        assertTrue(firstResult.isSuccess)

        // Attempt to start second trip while first is still IN_PROGRESS
        val secondResult = startTripUseCase(
            startAddress = "Bundaran HI, Jakarta",
            startLatitude = -6.195000,
            startLongitude = 106.823056,
            platformId = "gojek",
            categoryId = "food"
        )

        assertTrue(secondResult.isFailure)
        val exception = secondResult.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalStateException)
    }

    @Test
    fun persistsDirectQuoteWithoutTreatingItAsRealizedEarnings() = runTest {
        val result = startTripUseCase(
            startAddress = "Monas, Jakarta",
            startLatitude = -6.175392,
            startLongitude = 106.827153,
            platformId = "direct",
            categoryId = "passenger",
            quotedDistanceMeters = 20_000.0,
            quotedFareAmountCents = 8_000_00L
        )

        val trip = result.getOrThrow()

        assertEquals(20_000.0, trip.quotedDistanceMeters, 0.001)
        assertEquals(8_000_00L, trip.quotedFareAmountCents)
        assertEquals(0L, trip.platformFeeAmountCents)
    }
}
