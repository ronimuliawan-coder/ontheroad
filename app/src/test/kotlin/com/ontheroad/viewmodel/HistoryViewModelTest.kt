package com.ontheroad.viewmodel

import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.domain.usecase.GetTripHistoryUseCase
import com.ontheroad.core.domain.usecase.TripSortOrder
import com.ontheroad.core.model.RoutePoint
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private class TestHistoryFakeTripRepository : TripRepository {
    val tripsFlow = MutableStateFlow<Map<String, Trip>>(emptyMap())

    override suspend fun insertTrip(trip: Trip) {
        tripsFlow.value = tripsFlow.value + (trip.id to trip)
    }

    override suspend fun updateTrip(trip: Trip) {
        tripsFlow.value = tripsFlow.value + (trip.id to trip)
    }

    override suspend fun getTripById(id: String): Trip? = tripsFlow.value[id]

    override fun getActiveTrip(): Flow<Trip?> = tripsFlow.map { map ->
        map.values.firstOrNull { it.status == TripStatus.IN_PROGRESS }
    }

    override fun getAllTrips(): Flow<List<Trip>> = tripsFlow.map { it.values.toList() }
    override fun getTripsByShiftId(shiftId: String): Flow<List<Trip>> = tripsFlow.map { map ->
        map.values.filter { it.shiftId == shiftId }
    }
    override suspend fun insertRoutePoint(routePoint: RoutePoint) {}
    override fun getRoutePointsForTrip(tripId: String): Flow<List<RoutePoint>> = MutableStateFlow(emptyList())
    override suspend fun deleteTrip(id: String) {
        tripsFlow.value = tripsFlow.value - id
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tripRepository: TestHistoryFakeTripRepository
    private lateinit var getTripHistoryUseCase: GetTripHistoryUseCase
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tripRepository = TestHistoryFakeTripRepository()
        getTripHistoryUseCase = GetTripHistoryUseCase(tripRepository)
        viewModel = HistoryViewModel(getTripHistoryUseCase, tripRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun streamsTripsSortedByProfitability() = runTest(testDispatcher) {
        val trip1 = Trip(
            id = "t1",
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
        val trip2 = Trip(
            id = "t2",
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

        tripRepository.insertTrip(trip1)
        tripRepository.insertTrip(trip2)
        testDispatcher.scheduler.runCurrent()

        viewModel.setSortOrder(TripSortOrder.PROFITABILITY_HIGH_TO_LOW)
        testDispatcher.scheduler.runCurrent()

        val trips = viewModel.uiState.value.trips
        assertEquals(2, trips.size)
        assertEquals("t2", trips[0].id) // $4/km
        assertEquals("t1", trips[1].id) // $1/km
    }

    @Test
    fun deletesTripFromRepository() = runTest(testDispatcher) {
        val trip = Trip(
            id = "t-del",
            platformId = "grab",
            categoryId = "passenger",
            startTimeMillis = 1000,
            startAddress = "A",
            startLatitude = 0.0,
            startLongitude = 0.0,
            status = TripStatus.COMPLETED
        )
        tripRepository.insertTrip(trip)
        testDispatcher.scheduler.runCurrent()

        viewModel.deleteTrip("t-del")
        testDispatcher.scheduler.runCurrent()

        assertEquals(0, viewModel.uiState.value.trips.size)
    }
}
