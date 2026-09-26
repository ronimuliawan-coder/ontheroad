package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.FakeShiftRepository
import com.ontheroad.core.domain.repository.FakeTripRepository
import com.ontheroad.core.model.ShiftStatus
import com.ontheroad.core.model.TripStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartTripUseCaseTest {

    private lateinit var tripRepository: FakeTripRepository
    private lateinit var shiftRepository: FakeShiftRepository
    private lateinit var startTripUseCase: StartTripUseCase

    @Before
    fun setUp() {
        tripRepository = FakeTripRepository()
        shiftRepository = FakeShiftRepository()
        startTripUseCase = StartTripUseCase(tripRepository, shiftRepository)
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

    @Test
    fun bootstrapsActiveShiftWhenNoneExists() = runTest {
        val result = startTripUseCase(
            startAddress = "Monas, Jakarta",
            startLatitude = -6.175392,
            startLongitude = 106.827153,
            platformId = "grab",
            categoryId = "passenger"
        )

        val trip = result.getOrThrow()
        assertNotNull(trip.shiftId)

        val activeShift = shiftRepository.getActiveShift().first()
        assertNotNull(activeShift)
        assertEquals(trip.shiftId, activeShift?.id)
        assertEquals(ShiftStatus.ACTIVE, activeShift?.status)
    }

    @Test
    fun reusesExistingActiveShiftInsteadOfCreatingAnother() = runTest {
        val completeTripUseCase = CompleteTripUseCase(tripRepository)
        val first = startTripUseCase(
            startAddress = "Monas, Jakarta",
            startLatitude = -6.175392,
            startLongitude = 106.827153,
            platformId = "grab",
            categoryId = "passenger"
        ).getOrThrow()
        completeTripUseCase(
            tripId = first.id,
            endAddress = "Senayan, Jakarta",
            endLatitude = -6.225014,
            endLongitude = 106.799994
        ).getOrThrow()

        val second = startTripUseCase(
            startAddress = "Senayan, Jakarta",
            startLatitude = -6.225014,
            startLongitude = 106.799994,
            platformId = "grab",
            categoryId = "passenger"
        ).getOrThrow()

        assertEquals(first.shiftId, second.shiftId)
    }

    @Test
    fun preservesExplicitShiftIdWhenSupplied() = runTest {
        val result = startTripUseCase(
            startAddress = "Monas, Jakarta",
            startLatitude = -6.175392,
            startLongitude = 106.827153,
            platformId = "grab",
            categoryId = "passenger",
            shiftId = "explicit-shift"
        )

        assertEquals("explicit-shift", result.getOrThrow().shiftId)
        // No bootstrap shift may be created when the caller supplies one.
        assertNull(shiftRepository.getActiveShift().first())
    }
}
