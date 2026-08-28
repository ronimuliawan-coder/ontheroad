package com.ontheroad.core.data.repository

import com.ontheroad.core.data.local.dao.TripDao
import com.ontheroad.core.data.local.entity.TripEntity
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class TripRepositoryImplTest {

    private lateinit var tripDao: TripDao
    private lateinit var repository: TripRepositoryImpl

    @Before
    fun setUp() {
        tripDao = mockk(relaxed = true)
        repository = TripRepositoryImpl(tripDao)
    }

    @Test
    fun insertTripConvertsDomainToEntityAndDelegatesToDao() = runTest {
        val trip = Trip(
            id = "trip-1",
            platformId = "grab",
            categoryId = "passenger",
            startTimeMillis = 1000,
            startAddress = "Origin",
            startLatitude = -6.175,
            startLongitude = 106.827,
            status = TripStatus.IN_PROGRESS
        )

        repository.insertTrip(trip)

        coVerify(exactly = 1) {
            tripDao.insertTrip(match { it.id == "trip-1" && it.platformId == "grab" })
        }
    }

    @Test
    fun getTripByIdMapsEntityToDomain() = runTest {
        val entity = TripEntity(
            id = "trip-1",
            shiftId = "shift-1",
            platformId = "grab",
            categoryId = "passenger",
            startTimeMillis = 1000,
            endTimeMillis = 2000,
            startAddress = "Origin",
            endAddress = "Destination",
            startLatitude = -6.175,
            startLongitude = 106.827,
            endLatitude = -6.195,
            endLongitude = 106.823,
            actualDistanceMeters = 4000.0,
            quotedDistanceMeters = 3800.0,
            durationSeconds = 1000,
            platformFeeAmountCents = 2000,
            cashCollectedAmountCents = 500,
            tipAmountCents = 100,
            notes = "Great trip",
            status = "COMPLETED"
        )

        coEvery { tripDao.getTripById("trip-1") } returns entity

        val result = repository.getTripById("trip-1")

        assertNotNull(result)
        assertEquals("trip-1", result?.id)
        assertEquals(TripStatus.COMPLETED, result?.status)
        assertEquals(2600L, result?.totalEarningsCents) // 2000 + 500 + 100
        assertEquals(4.0, result?.actualDistanceKm ?: 0.0, 0.01)
    }

    @Test
    fun getActiveTripStreamsDomainTrip() = runTest {
        val entity = TripEntity(
            id = "trip-active",
            shiftId = null,
            platformId = "gojek",
            categoryId = "food",
            startTimeMillis = 5000,
            endTimeMillis = null,
            startAddress = "Restaurant",
            endAddress = null,
            startLatitude = 0.0,
            startLongitude = 0.0,
            endLatitude = null,
            endLongitude = null,
            actualDistanceMeters = 0.0,
            quotedDistanceMeters = null,
            durationSeconds = 0,
            platformFeeAmountCents = 0,
            cashCollectedAmountCents = 0,
            tipAmountCents = 0,
            notes = "",
            status = "IN_PROGRESS"
        )

        coEvery { tripDao.getActiveTrip() } returns flowOf(entity)

        val activeTrip = repository.getActiveTrip().first()

        assertNotNull(activeTrip)
        assertEquals("trip-active", activeTrip?.id)
        assertEquals(TripStatus.IN_PROGRESS, activeTrip?.status)
    }
}
