package com.ontheroad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.domain.usecase.GetTripHistoryUseCase
import com.ontheroad.core.domain.usecase.TripSortOrder
import com.ontheroad.core.model.Trip
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val trips: List<Trip> = emptyList(),
    val sortOrder: TripSortOrder = TripSortOrder.RECENT_FIRST,
    val platformFilter: String? = null,
    val categoryFilter: String? = null,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val getTripHistoryUseCase: GetTripHistoryUseCase,
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(TripSortOrder.RECENT_FIRST)
    private val _platformFilter = MutableStateFlow<String?>(null)
    private val _categoryFilter = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        observeTrips()
    }

    private fun observeTrips() {
        combine(_sortOrder, _platformFilter, _categoryFilter) { sort, platform, category ->
            Triple(sort, platform, category)
        }.flatMapLatest { (sort, platform, category) ->
            getTripHistoryUseCase(sort, platform, category)
        }.onEach { trips ->
            _uiState.update {
                it.copy(
                    trips = trips,
                    sortOrder = _sortOrder.value,
                    platformFilter = _platformFilter.value,
                    categoryFilter = _categoryFilter.value
                )
            }
        }.launchIn(viewModelScope)
    }

    fun setSortOrder(sortOrder: TripSortOrder) {
        _sortOrder.value = sortOrder
    }

    fun setPlatformFilter(platformId: String?) {
        _platformFilter.value = platformId
    }

    fun setCategoryFilter(categoryId: String?) {
        _categoryFilter.value = categoryId
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            tripRepository.deleteTrip(tripId)
        }
    }
}
