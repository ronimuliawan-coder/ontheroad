package com.ontheroad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.domain.usecase.CompleteTripUseCase
import com.ontheroad.core.domain.usecase.StartTripUseCase
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
    val errorMessage: String? = null
)

class TrackerViewModel(
    private val tripRepository: TripRepository,
    private val startTripUseCase: StartTripUseCase,
    private val completeTripUseCase: CompleteTripUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        observeActiveTrip()
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
