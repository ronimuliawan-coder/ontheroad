package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.FakeTripRepository
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetTripHistoryUseCaseTest {

    private lateinit var tripRepository: FakeTripRepository
    private lateinit var getTripHistoryUseCase: GetTripHistoryUseCase

    @Before
    fun setUp() {
        tripRepository = FakeTripRepository()
        getTripHistoryUseCase = GetTripHistoryUseCase(tripRepository)
    }

    @Test
    fun sortsTripsByProfitabilityDescending() = runTest {
        // Trip 1: 10km, earned $10 -> $1.00/km (100 cents/km)
        val trip1 = Trip(
            id = "trip-1",
            platformId = "grab",
            categoryId = "passenger",
            startTimeMillis = 1000,
            startAddress = "A",
            startLatitude = 0.0,
            startLongitude = 0.0,
            actualDistanceMeters = 10000.0,
            platformFeeAmountCents = 1000,
            status = TripStatus.COMPLETED
        )

        // Trip 2: 2km, earned $8 -> $4.00/km (400 cents/km) - Highest profitability
        val trip2 = Trip(
            id = "trip-2",
            platformId = "gojek",
            categoryId = "food",
            startTimeMillis = 2000,
            startAddress = "B",
            startLatitude = 0.0,
            startLongitude = 0.0,
            actualDistanceMeters = 2000.0,
            platformFeeAmountCents = 800,
            status = TripStatus.COMPLETED
        )

        // Trip 3: 5km, earned $10 -> $2.00/km (200 cents/km)
        val trip3 = Trip(
            id = "trip-3",
            platformId = "uber",
            categoryId = "passenger",
            startTimeMillis = 3000,
            startAddress = "C",
            startLatitude = 0.0,
            startLongitude = 0.0,
            actualDistanceMeters = 5000.0,
            platformFeeAmountCents = 1000,
            status = TripStatus.COMPLETED
        )

        tripRepository.insertTrip(trip1)
        tripRepository.insertTrip(trip2)
        tripRepository.insertTrip(trip3)

        val sorted = getTripHistoryUseCase(
            sortOrder = TripSortOrder.PROFITABILITY_HIGH_TO_LOW
        ).first()

        assertEquals(3, sorted.size)
        assertEquals("trip-2", sorted[0].id) // $4/km
        assertEquals("trip-3", sorted[1].id) // $2/km
        assertEquals("trip-1", sorted[2].id) // $1/km
    }

    @Test
    fun filtersTripsByPlatformAndCategory() = runTest {
        val grabPassenger = Trip(
            id = "t1",
            platformId = "grab",
            categoryId = "passenger",
            startTimeMillis = 1000,
            startAddress = "A",
            startLatitude = 0.0,
            startLongitude = 0.0,
            status = TripStatus.COMPLETED
        )
        val grabFood = Trip(
            id = "t2",
            platformId = "grab",
            categoryId = "food",
            startTimeMillis = 2000,
            startAddress = "B",
            startLatitude = 0.0,
            startLongitude = 0.0,
            status = TripStatus.COMPLETED
        )
        val gojekFood = Trip(
            id = "t3",
            platformId = "gojek",
            categoryId = "food",
            startTimeMillis = 3000,
            startAddress = "C",
            startLatitude = 0.0,
            startLongitude = 0.0,
            status = TripStatus.COMPLETED
        )

        tripRepository.insertTrip(grabPassenger)
        tripRepository.insertTrip(grabFood)
        tripRepository.insertTrip(gojekFood)

        val filtered = getTripHistoryUseCase(
            platformIdFilter = "grab",
            categoryIdFilter = "food"
        ).first()

        assertEquals(1, filtered.size)
        assertEquals("t2", filtered[0].id)
    }
}
