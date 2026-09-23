package com.ontheroad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.ontheroad.core.model.Platform
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class TrackerUiState(
    val isTracking: Boolean = false,
    val activeTrip: Trip? = null,
    val actualDistanceKm: Double = 0.0,
    val durationSeconds: Long = 0,
    val speedKmh: Float = 0f,
    val selectedPlatformId: String = "grab",
    val selectedCategoryId: String = "passenger",
    val platforms: List<Platform> = emptyList(),
    val showCompleteModal: Boolean = false,
    val errorMessage: String? = null,
    val directPickupAddress: String = "Current GPS Location",
    val directPickupLatitude: Double = -6.175392,
    val directPickupLongitude: Double = 106.827153,
    val directPickupSuggestions: List<AddressSuggestion> = emptyList(),
    val directDestinationAddress: String = "",
    val directDestinationLatitude: Double? = null,
    val directDestinationLongitude: Double? = null,
    val directDestinationSuggestions: List<AddressSuggestion> = emptyList(),
    val isSearchingAddress: Boolean = false,
    val addressLookupUnavailable: Boolean = false,
    val locationPermissionDenied: Boolean = false,
    val directEstimatedDistanceKmText: String = "",
    val isDistanceAutoCalculated: Boolean = false,
    val directCustomFareOverrideText: String = "",
    val directPricingRates: DirectPricingRates = DirectPricingRates(),
    val directCalculatedFareCents: Long = 15_000_00L
)

class TrackerViewModel(
    private val tripRepository: TripRepository,
    private val startTripUseCase: StartTripUseCase,
    private val completeTripUseCase: CompleteTripUseCase,
    private val userPreferencesRepository: UserPreferencesRepository? = null,
    private val calculateDirectFareUseCase: CalculateDirectFareUseCase = CalculateDirectFareUseCase(),
    private val estimateDistanceUseCase: EstimateDistanceUseCase = EstimateDistanceUseCase(),
    private val searchAddressUseCase: SearchAddressUseCase? = null,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var pickupSearchJob: Job? = null
    private var destinationSearchJob: Job? = null

    init {
        observeActiveTrip()
        observeDirectPricingRates()
        acquireCurrentLocation(showFallback = false)
    }

    private fun observeDirectPricingRates() {
        userPreferencesRepository?.getDirectPricingRates()
            ?.onEach { rates ->
                _uiState.update { current ->
                    val distanceKm = current.directEstimatedDistanceKmText.toDoubleOrNull() ?: 0.0
                    val overrideCents = current.directCustomFareOverrideText.toDoubleOrNull()?.let { (it * 100).toLong() }
                    val fare = calculateDirectFareUseCase(
                        distanceKm = distanceKm,
                        rates = rates,
                        customFareOverrideCents = overrideCents
                    )
                    current.copy(
                        directPricingRates = rates,
                        directCalculatedFareCents = fare
                    )
                }
            }
            ?.launchIn(viewModelScope)
    }

    private fun observeActiveTrip() {
        tripRepository.getActiveTrip()
            .onEach { trip ->
                if (trip != null && trip.status == TripStatus.IN_PROGRESS) {
                    _uiState.update {
                        it.copy(
                            isTracking = true,
                            activeTrip = trip,
                            actualDistanceKm = trip.actualDistanceKm,
                            selectedPlatformId = trip.platformId,
                            selectedCategoryId = trip.categoryId
                        )
                    }
                    startDurationTimer(trip.startTimeMillis)
                } else {
                    stopDurationTimer()
                    _uiState.update {
                        it.copy(
                            isTracking = false,
                            activeTrip = null,
                            actualDistanceKm = 0.0,
                            durationSeconds = 0
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun startDurationTimer(startTimeMillis: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val elapsedSeconds = maxOf(0L, (System.currentTimeMillis() - startTimeMillis) / 1000)
                _uiState.update { it.copy(durationSeconds = elapsedSeconds) }
                delay(1000L)
            }
        }
    }

    fun stopDurationTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    public override fun onCleared() {
        super.onCleared()
        stopDurationTimer()
        pickupSearchJob?.cancel()
        destinationSearchJob?.cancel()
    }

    fun selectPlatform(platformId: String) {
        if (!_uiState.value.isTracking) {
            _uiState.update { it.copy(selectedPlatformId = platformId) }
        }
    }

    fun selectCategory(categoryId: String) {
        if (!_uiState.value.isTracking) {
            _uiState.update { it.copy(selectedCategoryId = categoryId) }
        }
    }

    fun acquireCurrentLocation(showFallback: Boolean = true) {
        if (getCurrentLocationUseCase == null) {
            if (showFallback) {
                _uiState.update { it.copy(addressLookupUnavailable = true) }
            }
            return
        }
        viewModelScope.launch {
            val location = getCurrentLocationUseCase.invoke()
            if (location != null) {
                _uiState.update { current ->
                    val updatedState = current.copy(
                        directPickupAddress = location.fullAddress,
                        directPickupLatitude = location.latitude,
                        directPickupLongitude = location.longitude,
                        addressLookupUnavailable = false
                    )
                    recalculateDistanceAndFare(updatedState)
                }
            } else if (showFallback) {
                _uiState.update { it.copy(addressLookupUnavailable = true) }
            }
        }
    }

    fun reportLocationPermissionDenied() {
        _uiState.update { it.copy(locationPermissionDenied = true) }
    }

    fun clearLocationPermissionMessage() {
        _uiState.update { it.copy(locationPermissionDenied = false) }
    }

    fun updateDirectPickupAddress(address: String) {
        _uiState.update {
            it.copy(
                directPickupAddress = address,
                addressLookupUnavailable = false
            )
        }
        pickupSearchJob?.cancel()

        if (searchAddressUseCase == null || address.trim().length < 2) {
            _uiState.update { it.copy(directPickupSuggestions = emptyList(), isSearchingAddress = false) }
            return
        }

        pickupSearchJob = viewModelScope.launch {
            delay(350L)
            _uiState.update { it.copy(isSearchingAddress = true) }
            val suggestions = searchAddressUseCase.invoke(
                query = address,
                biasLatitude = _uiState.value.directPickupLatitude,
                biasLongitude = _uiState.value.directPickupLongitude
            )
            _uiState.update {
                it.copy(
                    directPickupSuggestions = suggestions,
                    isSearchingAddress = false,
                    addressLookupUnavailable = suggestions.isEmpty()
                )
            }
        }
    }

    fun selectPickupSuggestion(suggestion: AddressSuggestion) {
        pickupSearchJob?.cancel()
        _uiState.update { current ->
            val updatedState = current.copy(
                directPickupAddress = suggestion.fullAddress,
                directPickupLatitude = suggestion.latitude,
                directPickupLongitude = suggestion.longitude,
                directPickupSuggestions = emptyList(),
                addressLookupUnavailable = false
            )
            recalculateDistanceAndFare(updatedState)
        }
    }

    fun updateDirectDestinationAddress(address: String) {
        _uiState.update {
            it.copy(
                directDestinationAddress = address,
                addressLookupUnavailable = false
            )
        }
        destinationSearchJob?.cancel()

        if (searchAddressUseCase == null || address.trim().length < 2) {
            _uiState.update { it.copy(directDestinationSuggestions = emptyList(), isSearchingAddress = false) }
            return
        }

        destinationSearchJob = viewModelScope.launch {
            delay(350L)
            _uiState.update { it.copy(isSearchingAddress = true) }
            val suggestions = searchAddressUseCase.invoke(
                query = address,
                biasLatitude = _uiState.value.directPickupLatitude,
                biasLongitude = _uiState.value.directPickupLongitude
            )
            _uiState.update { current ->
                val autoUpdated = if (suggestions.isNotEmpty()) {
                    // Auto-resolve top match
                    val first = suggestions.first()
                    val distMeters = estimateDistanceUseCase(
                        startLat = current.directPickupLatitude,
                        startLng = current.directPickupLongitude,
                        endLat = first.latitude,
                        endLng = first.longitude,
                        roadDetourMultiplier = current.directPricingRates.roadDetourMultiplier
                    )
                    val distKm = distMeters / 1000.0
                    val distanceText = String.format(Locale.US, "%.1f", distKm)
                    val overrideCents = current.directCustomFareOverrideText.toDoubleOrNull()?.let { (it * 100).toLong() }
                    val fare = calculateDirectFareUseCase(
                        distanceKm = distKm,
                        rates = current.directPricingRates,
                        customFareOverrideCents = overrideCents
                    )
                    current.copy(
                        directDestinationSuggestions = suggestions,
                        directDestinationLatitude = first.latitude,
                        directDestinationLongitude = first.longitude,
                        directEstimatedDistanceKmText = distanceText,
                        isDistanceAutoCalculated = true,
                        directCalculatedFareCents = fare,
                        isSearchingAddress = false,
                        addressLookupUnavailable = false
                    )
                } else {
                    current.copy(
                        directDestinationSuggestions = suggestions,
                        isSearchingAddress = false,
                        addressLookupUnavailable = suggestions.isEmpty()
                    )
                }
                autoUpdated
            }
        }
    }

    fun selectDestinationSuggestion(suggestion: AddressSuggestion) {
        destinationSearchJob?.cancel()
        _uiState.update { current ->
            val distMeters = estimateDistanceUseCase(
                startLat = current.directPickupLatitude,
                startLng = current.directPickupLongitude,
                endLat = suggestion.latitude,
                endLng = suggestion.longitude,
                roadDetourMultiplier = current.directPricingRates.roadDetourMultiplier
            )
            val distKm = distMeters / 1000.0
            val distanceText = String.format(Locale.US, "%.1f", distKm)
            val overrideCents = current.directCustomFareOverrideText.toDoubleOrNull()?.let { (it * 100).toLong() }
            val fare = calculateDirectFareUseCase(
                distanceKm = distKm,
                rates = current.directPricingRates,
                customFareOverrideCents = overrideCents
            )
            current.copy(
                directDestinationAddress = suggestion.fullAddress,
                directDestinationLatitude = suggestion.latitude,
                directDestinationLongitude = suggestion.longitude,
                directDestinationSuggestions = emptyList(),
                directEstimatedDistanceKmText = distanceText,
                isDistanceAutoCalculated = true,
                directCalculatedFareCents = fare,
                addressLookupUnavailable = false
            )
        }
    }

    private fun recalculateDistanceAndFare(state: TrackerUiState): TrackerUiState {
        val destLat = state.directDestinationLatitude
        val destLon = state.directDestinationLongitude
        return if (destLat != null && destLon != null) {
            val distMeters = estimateDistanceUseCase(
                startLat = state.directPickupLatitude,
                startLng = state.directPickupLongitude,
                endLat = destLat,
                endLng = destLon,
                roadDetourMultiplier = state.directPricingRates.roadDetourMultiplier
            )
            val distKm = distMeters / 1000.0
            val distanceText = String.format(Locale.US, "%.1f", distKm)
            val overrideCents = state.directCustomFareOverrideText.toDoubleOrNull()?.let { (it * 100).toLong() }
            val fare = calculateDirectFareUseCase(
                distanceKm = distKm,
                rates = state.directPricingRates,
                customFareOverrideCents = overrideCents
            )
            state.copy(
                directEstimatedDistanceKmText = distanceText,
                isDistanceAutoCalculated = true,
                directCalculatedFareCents = fare
            )
        } else {
            state
        }
    }

    fun updateDirectEstimatedDistance(distanceKmText: String) {
        _uiState.update { current ->
            val distanceKm = distanceKmText.toDoubleOrNull() ?: 0.0
            val overrideCents = current.directCustomFareOverrideText.toDoubleOrNull()?.let { (it * 100).toLong() }
            val fare = calculateDirectFareUseCase(
                distanceKm = distanceKm,
                rates = current.directPricingRates,
                customFareOverrideCents = overrideCents
            )
            current.copy(
                directEstimatedDistanceKmText = distanceKmText,
                isDistanceAutoCalculated = false,
                directCalculatedFareCents = fare
            )
        }
    }

    fun updateDirectCustomFareOverride(overrideText: String) {
        _uiState.update { current ->
            val distanceKm = current.directEstimatedDistanceKmText.toDoubleOrNull() ?: 0.0
            val overrideCents = overrideText.toDoubleOrNull()?.let { (it * 100).toLong() }
            val fare = calculateDirectFareUseCase(
                distanceKm = distanceKm,
                rates = current.directPricingRates,
                customFareOverrideCents = overrideCents
            )
            current.copy(
                directCustomFareOverrideText = overrideText,
                directCalculatedFareCents = fare
            )
        }
    }

    fun startTrip(
        startAddress: String,
        startLatitude: Double,
        startLongitude: Double,
        shiftId: String? = null,
        onSuccess: (Trip) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = startTripUseCase(
                startAddress = startAddress,
                startLatitude = startLatitude,
                startLongitude = startLongitude,
                platformId = _uiState.value.selectedPlatformId,
                categoryId = _uiState.value.selectedCategoryId,
                shiftId = shiftId
            )
            result.onSuccess { trip ->
                onSuccess(trip)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun startDirectTrip(
        startAddress: String,
        startLatitude: Double,
        startLongitude: Double,
        shiftId: String? = null,
        onSuccess: (Trip) -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val distanceKm = currentState.directEstimatedDistanceKmText.toDoubleOrNull()
            val quotedDistanceMeters = distanceKm?.let { it * 1000.0 }
            val quotedFareCents = currentState.directCalculatedFareCents

            val result = startTripUseCase(
                startAddress = startAddress.ifBlank { currentState.directPickupAddress },
                startLatitude = if (startLatitude != 0.0) startLatitude else currentState.directPickupLatitude,
                startLongitude = if (startLongitude != 0.0) startLongitude else currentState.directPickupLongitude,
                platformId = "direct",
                categoryId = currentState.selectedCategoryId,
                shiftId = shiftId,
                quotedDistanceMeters = quotedDistanceMeters,
                quotedFareAmountCents = quotedFareCents,
                endAddress = currentState.directDestinationAddress.ifBlank { null }
            )
            result.onSuccess { trip ->
                onSuccess(trip)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun openCompleteModal() {
        _uiState.update { it.copy(showCompleteModal = true) }
    }

    fun dismissCompleteModal() {
        _uiState.update { it.copy(showCompleteModal = false) }
    }

    fun completeTrip(
        endAddress: String,
        endLatitude: Double,
        endLongitude: Double,
        platformFeeAmountCents: Long,
        cashCollectedAmountCents: Long,
        tipAmountCents: Long = 0,
        quotedDistanceMeters: Double? = null,
        notes: String = "",
        onCompleted: (Trip) -> Unit = {}
    ) {
        val activeTrip = _uiState.value.activeTrip ?: return
        viewModelScope.launch {
            val result = completeTripUseCase(
                tripId = activeTrip.id,
                endAddress = endAddress,
                endLatitude = endLatitude,
                endLongitude = endLongitude,
                platformFeeAmountCents = platformFeeAmountCents,
                cashCollectedAmountCents = cashCollectedAmountCents,
                tipAmountCents = tipAmountCents,
                quotedDistanceMeters = quotedDistanceMeters,
                notes = notes
            )
            result.onSuccess { completedTrip ->
                _uiState.update { it.copy(showCompleteModal = false) }
                onCompleted(completedTrip)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
