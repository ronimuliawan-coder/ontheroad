package com.ontheroad.viewmodel

import app.cash.turbine.test
import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.domain.usecase.CompleteTripUseCase
import com.ontheroad.core.domain.usecase.StartTripUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class TestFakeTripRepository : TripRepository {
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
class TrackerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tripRepository: TestFakeTripRepository
    private lateinit var startTripUseCase: StartTripUseCase
    private lateinit var completeTripUseCase: CompleteTripUseCase
    private lateinit var viewModel: TrackerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tripRepository = TestFakeTripRepository()
        startTripUseCase = StartTripUseCase(tripRepository)
        completeTripUseCase = CompleteTripUseCase(tripRepository)
        viewModel = TrackerViewModel(tripRepository, startTripUseCase, completeTripUseCase)
    }

    @After
    fun tearDown() {
        viewModel.stopDurationTimer()
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiStateHasTrackingFalse() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isTracking)
            assertEquals(0.0, state.actualDistanceKm, 0.001)
            assertEquals("grab", state.selectedPlatformId)
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.stopDurationTimer()
    }

    @Test
    fun selectPlatformUpdatesUiStateWhenNotTracking() = runTest(testDispatcher) {
        viewModel.selectPlatform("gojek")
        assertEquals("gojek", viewModel.uiState.value.selectedPlatformId)
    }

    @Test
    fun startingTripTransitionsUiStateToTracking() = runTest(testDispatcher) {
        viewModel.selectPlatform("gojek")
        viewModel.startTrip(
            startAddress = "Monas",
            startLatitude = -6.175,
            startLongitude = 106.827
        )
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isTracking)
        assertEquals("gojek", viewModel.uiState.value.selectedPlatformId)
        viewModel.stopDurationTimer()
    }

    @Test
    fun completingTripTransitionsUiStateToIdle() = runTest(testDispatcher) {
        viewModel.startTrip(
            startAddress = "Monas",
            startLatitude = -6.175,
            startLongitude = 106.827
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.completeTrip(
            endAddress = "Bundaran HI",
            endLatitude = -6.195,
            endLongitude = 106.823,
            platformFeeAmountCents = 2500,
            cashCollectedAmountCents = 1000,
            quotedDistanceMeters = 4000.0
        )
        testDispatcher.scheduler.runCurrent()

        assertFalse(viewModel.uiState.value.isTracking)
    }
}
