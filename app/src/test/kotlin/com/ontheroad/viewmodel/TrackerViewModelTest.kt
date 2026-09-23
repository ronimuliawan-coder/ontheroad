package com.ontheroad.viewmodel

import app.cash.turbine.test
import com.ontheroad.core.domain.repository.LocationRepository
import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.domain.repository.UserPreferencesRepository
import com.ontheroad.core.domain.usecase.CalculateDirectFareUseCase
import com.ontheroad.core.domain.usecase.CompleteTripUseCase
import com.ontheroad.core.domain.usecase.EstimateDistanceUseCase
import com.ontheroad.core.domain.usecase.GetCurrentLocationUseCase
import com.ontheroad.core.domain.usecase.SearchAddressUseCase
import com.ontheroad.core.domain.usecase.StartTripUseCase
import com.ontheroad.core.model.AddressSuggestion
import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.model.DirectPricingProfile
import com.ontheroad.core.model.DirectPricingProfileSettings
import com.ontheroad.core.model.RoutePoint
import com.ontheroad.core.model.ThemeMode
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

private class TrackerFakeTripRepository : TripRepository {
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

private class TrackerFakeUserPreferencesRepository : UserPreferencesRepository {
    val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)
    val directPricingRatesFlow = MutableStateFlow(DirectPricingRates())
    val directPricingProfilesFlow = MutableStateFlow(DirectPricingProfileSettings())

    override fun getThemeMode(): Flow<ThemeMode> = themeModeFlow
    override suspend fun setThemeMode(mode: ThemeMode) { themeModeFlow.value = mode }
    override fun getDirectPricingRates(): Flow<DirectPricingRates> = directPricingRatesFlow
    override suspend fun setDirectPricingRates(rates: DirectPricingRates) {
        directPricingRatesFlow.value = rates
        val settings = directPricingProfilesFlow.value
        directPricingProfilesFlow.value = settings.copy(
            profiles = settings.profiles.map { profile ->
                if (profile.id == settings.activeProfileId) profile.copy(rates = rates) else profile
            }
        )
    }
    override fun getDirectPricingProfileSettings(): Flow<DirectPricingProfileSettings> = directPricingProfilesFlow
    override suspend fun setActiveDirectPricingProfile(profileId: String) {
        directPricingProfilesFlow.value = directPricingProfilesFlow.value.copy(activeProfileId = profileId)
        directPricingRatesFlow.value = directPricingProfilesFlow.value.activeProfile.rates
    }
    override suspend fun saveDirectPricingProfile(profile: DirectPricingProfile) {
        val settings = directPricingProfilesFlow.value
        directPricingProfilesFlow.value = settings.copy(
            profiles = settings.profiles.filterNot { it.id == profile.id } + profile
        )
    }
    override suspend fun deleteDirectPricingProfile(profileId: String) {
        val settings = directPricingProfilesFlow.value
        val profiles = settings.profiles.filterNot { it.id == profileId }
        directPricingProfilesFlow.value = DirectPricingProfileSettings(profiles, profiles.first().id)
    }
}

private class TrackerFakeLocationRepository : LocationRepository {
    var currentLocation: AddressSuggestion? = AddressSuggestion(
        title = "Bundaran HI",
        fullAddress = "Bundaran HI, Menteng, Jakarta Pusat",
        latitude = -6.195000,
        longitude = 106.823056
    )

    var searchResults = listOf(
        AddressSuggestion(
            title = "Soekarno-Hatta Airport",
            fullAddress = "Soekarno-Hatta Airport, Tangerang",
            latitude = -6.125556,
            longitude = 106.655833
        ),
        AddressSuggestion(
            title = "Monas",
            fullAddress = "Monas, Gambir, Jakarta Pusat",
            latitude = -6.175392,
            longitude = 106.827153
        )
    )

    override suspend fun getCurrentLocation(): AddressSuggestion? = currentLocation

    override suspend fun searchAddresses(
        query: String,
        biasLatitude: Double?,
        biasLongitude: Double?,
        maxResults: Int
    ): List<AddressSuggestion> {
        return searchResults.filter {
            it.title.contains(query, ignoreCase = true) || it.fullAddress.contains(query, ignoreCase = true)
        }
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): String? = "Jakarta, Indonesia"
}

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tripRepository: TrackerFakeTripRepository
    private lateinit var preferencesRepository: TrackerFakeUserPreferencesRepository
    private lateinit var locationRepository: TrackerFakeLocationRepository
    private lateinit var startTripUseCase: StartTripUseCase
    private lateinit var completeTripUseCase: CompleteTripUseCase
    private lateinit var searchAddressUseCase: SearchAddressUseCase
    private lateinit var getCurrentLocationUseCase: GetCurrentLocationUseCase
    private lateinit var viewModel: TrackerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tripRepository = TrackerFakeTripRepository()
        preferencesRepository = TrackerFakeUserPreferencesRepository()
        locationRepository = TrackerFakeLocationRepository()
        startTripUseCase = StartTripUseCase(tripRepository)
        completeTripUseCase = CompleteTripUseCase(tripRepository)
        searchAddressUseCase = SearchAddressUseCase(locationRepository)
        getCurrentLocationUseCase = GetCurrentLocationUseCase(locationRepository)

        viewModel = TrackerViewModel(
            tripRepository = tripRepository,
            startTripUseCase = startTripUseCase,
            completeTripUseCase = completeTripUseCase,
            userPreferencesRepository = preferencesRepository,
            searchAddressUseCase = searchAddressUseCase,
            getCurrentLocationUseCase = getCurrentLocationUseCase
        )
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

    @Test
    fun directBookingCalculatesFareAndStartsDirectTripWithQuote() = runTest(testDispatcher) {
        viewModel.selectPlatform("direct")
        viewModel.updateDirectPickupAddress("Hotel Indonesia")
        viewModel.updateDirectDestinationAddress("Soekarno Hatta Airport")
        viewModel.updateDirectEstimatedDistance("20.0")
        testDispatcher.scheduler.runCurrent()

        // Rates: Base 10.000 + 20km * 3.500 = 80.000 (80_000_00 cents)
        val uiState = viewModel.uiState.value
        assertEquals("Hotel Indonesia", uiState.directPickupAddress)
        assertEquals("Soekarno Hatta Airport", uiState.directDestinationAddress)
        assertEquals("20.0", uiState.directEstimatedDistanceKmText)
        assertEquals(80_000_00L, uiState.directCalculatedFareCents)

        // Start direct run
        var startedTrip: Trip? = null
        viewModel.startDirectTrip(
            startAddress = "Hotel Indonesia",
            startLatitude = -6.195,
            startLongitude = 106.823,
            onSuccess = { startedTrip = it }
        )
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isTracking)
        assertEquals("direct", viewModel.uiState.value.selectedPlatformId)
        assertEquals(20_000.0, startedTrip?.quotedDistanceMeters ?: 0.0, 0.1)
        assertEquals(80_000_00L, startedTrip?.quotedFareAmountCents)
        assertEquals(0L, startedTrip?.platformFeeAmountCents)
        assertEquals("Soekarno Hatta Airport", startedTrip?.endAddress)

        viewModel.completeTrip(
            endAddress = "Soekarno Hatta Airport",
            endLatitude = -6.125556,
            endLongitude = 106.655833,
            platformFeeAmountCents = 0L,
            cashCollectedAmountCents = 80_000_00L,
            quotedDistanceMeters = 20_000.0
        )
        testDispatcher.scheduler.runCurrent()

        val completedTrip = tripRepository.getTripById(startedTrip!!.id)
        assertEquals(80_000_00L, completedTrip?.quotedFareAmountCents)
        assertEquals(80_000_00L, completedTrip?.customerPaidTotalAmountCents)
        assertEquals(80_000_00L, completedTrip?.cashCollectedAmountCents)
        assertEquals(80_000_00L, completedTrip?.totalEarningsCents)

        viewModel.stopDurationTimer()
    }

    @Test
    fun selectingRateProfileRecalculatesAutoEstimatedRoadDistance() = runTest(testDispatcher) {
        val rates = DirectPricingRates(roadDetourMultiplier = 1.0)
        val motorcycleRates = rates.copy(roadDetourMultiplier = 2.0)
        preferencesRepository.directPricingProfilesFlow.value = DirectPricingProfileSettings(
            profiles = listOf(
                DirectPricingProfile("car", "Car / Passenger", rates),
                DirectPricingProfile("motorcycle", "Motorcycle / Courier", motorcycleRates)
            ),
            activeProfileId = "car"
        )
        testDispatcher.scheduler.runCurrent()

        viewModel.selectPlatform("direct")
        viewModel.selectDestinationSuggestion(
            AddressSuggestion(
                title = "Airport",
                fullAddress = "Soekarno-Hatta Airport",
                latitude = -6.125556,
                longitude = 106.655833
            )
        )
        val carDistance = viewModel.uiState.value.directEstimatedDistanceKmText.toDouble()
        assertEquals("car", viewModel.uiState.value.activeDirectPricingProfileId)

        viewModel.selectDirectPricingProfile("motorcycle")
        testDispatcher.scheduler.runCurrent()

        val motorcycleDistance = viewModel.uiState.value.directEstimatedDistanceKmText.toDouble()
        assertEquals("motorcycle", viewModel.uiState.value.activeDirectPricingProfileId)
        assertTrue(motorcycleDistance > carDistance * 1.9)
    }

    @Test
    fun destinationSearchAutomaticallyResolvesCoordinatesAndCalculatesDistanceAndFare() = runTest(testDispatcher) {
        viewModel.selectPlatform("direct")
        // Type "Airport" to trigger debounced search
        viewModel.updateDirectDestinationAddress("Airport")
        testDispatcher.scheduler.advanceTimeBy(400L)
        testDispatcher.scheduler.runCurrent()

        val uiState = viewModel.uiState.value
        assertEquals("Airport", uiState.directDestinationAddress)
        assertTrue(uiState.isDistanceAutoCalculated)
        assertTrue(uiState.directEstimatedDistanceKmText.isNotBlank())
        assertTrue((uiState.directEstimatedDistanceKmText.toDoubleOrNull() ?: 0.0) > 10.0)
        assertTrue(uiState.directCalculatedFareCents > 15_000_00L)

        viewModel.stopDurationTimer()
    }

    @Test
    fun selectingDestinationSuggestionCalculatesDistanceAccurately() = runTest(testDispatcher) {
        viewModel.selectPlatform("direct")
        val airportSuggestion = AddressSuggestion(
            title = "Soekarno-Hatta Airport",
            fullAddress = "Soekarno-Hatta Airport, Tangerang",
            latitude = -6.125556,
            longitude = 106.655833
        )
        viewModel.selectDestinationSuggestion(airportSuggestion)
        testDispatcher.scheduler.runCurrent()

        val uiState = viewModel.uiState.value
        assertEquals("Soekarno-Hatta Airport, Tangerang", uiState.directDestinationAddress)
        assertTrue(uiState.isDistanceAutoCalculated)
        assertEquals(-6.125556, uiState.directDestinationLatitude ?: 0.0, 0.0001)
        assertTrue((uiState.directEstimatedDistanceKmText.toDoubleOrNull() ?: 0.0) > 0.0)
        assertTrue(uiState.directCalculatedFareCents > 0L)

        viewModel.stopDurationTimer()
    }

    @Test
    fun directBookingSupportsCustomFareOverride() = runTest(testDispatcher) {
        viewModel.updateDirectEstimatedDistance("10.0")
        viewModel.updateDirectCustomFareOverride("100000") // Rp 100.000 override
        testDispatcher.scheduler.runCurrent()

        assertEquals(100_000_00L, viewModel.uiState.value.directCalculatedFareCents)
        viewModel.stopDurationTimer()
    }

    @Test
    fun updatedRatesRecalculateCockpitFareAndRetainManualOverride() = runTest(testDispatcher) {
        viewModel.updateDirectEstimatedDistance("2.0")
        testDispatcher.scheduler.runCurrent()
        assertEquals(17_000_00L, viewModel.uiState.value.directCalculatedFareCents)

        preferencesRepository.setDirectPricingRates(
            DirectPricingRates(
                baseFareAmountCents = 20_000_00L,
                ratePerKmAmountCents = 1_000_00L,
                minimumFareAmountCents = 15_000_00L
            )
        )
        testDispatcher.scheduler.runCurrent()
        assertEquals(22_000_00L, viewModel.uiState.value.directCalculatedFareCents)

        viewModel.updateDirectCustomFareOverride("50000")
        testDispatcher.scheduler.runCurrent()
        preferencesRepository.setDirectPricingRates(
            DirectPricingRates(
                baseFareAmountCents = 1_000_00L,
                ratePerKmAmountCents = 1_000_00L,
                minimumFareAmountCents = 2_000_00L
            )
        )
        testDispatcher.scheduler.runCurrent()

        assertEquals(50_000_00L, viewModel.uiState.value.directCalculatedFareCents)
        viewModel.stopDurationTimer()
    }

    @Test
    fun emptyAddressSearchKeepsManualDistanceFallbackAvailable() = runTest(testDispatcher) {
        locationRepository.searchResults = emptyList()
        viewModel.selectPlatform("direct")

        viewModel.updateDirectDestinationAddress("Unknown destination")
        testDispatcher.scheduler.advanceTimeBy(400L)
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value.addressLookupUnavailable)
        assertFalse(viewModel.uiState.value.isDistanceAutoCalculated)
        viewModel.stopDurationTimer()
    }
}
