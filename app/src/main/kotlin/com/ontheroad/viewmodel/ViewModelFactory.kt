package com.ontheroad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ontheroad.OnTheRoadApplication
import com.ontheroad.core.data.repository.ShiftRepositoryImpl
import com.ontheroad.core.data.repository.TripRepositoryImpl
import com.ontheroad.core.domain.usecase.CompleteTripUseCase
import com.ontheroad.core.domain.usecase.GetShiftSummaryUseCase
import com.ontheroad.core.domain.usecase.GetTripHistoryUseCase
import com.ontheroad.core.domain.usecase.StartTripUseCase

class ViewModelFactory(
    private val application: OnTheRoadApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = application.database
        val tripRepository = TripRepositoryImpl(database.tripDao())
        val shiftRepository = ShiftRepositoryImpl(database.shiftDao())

        return when {
            modelClass.isAssignableFrom(TrackerViewModel::class.java) -> {
                val startTripUseCase = StartTripUseCase(tripRepository)
                val completeTripUseCase = CompleteTripUseCase(tripRepository)
                TrackerViewModel(tripRepository, startTripUseCase, completeTripUseCase) as T
            }
            modelClass.isAssignableFrom(ShiftViewModel::class.java) -> {
                val getShiftSummaryUseCase = GetShiftSummaryUseCase(tripRepository, shiftRepository)
                ShiftViewModel(shiftRepository, getShiftSummaryUseCase) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                val getTripHistoryUseCase = GetTripHistoryUseCase(tripRepository)
                HistoryViewModel(getTripHistoryUseCase, tripRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
