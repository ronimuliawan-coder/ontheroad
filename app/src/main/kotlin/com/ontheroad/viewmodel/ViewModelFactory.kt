package com.ontheroad.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ontheroad.OnTheRoadApplication
import com.ontheroad.core.data.repository.LocationRepositoryImpl
import com.ontheroad.core.data.repository.ShiftRepositoryImpl
import com.ontheroad.core.data.repository.TripRepositoryImpl
import com.ontheroad.core.data.repository.UserPreferencesRepositoryImpl
import com.ontheroad.core.domain.usecase.CalculateDirectFareUseCase
import com.ontheroad.core.domain.usecase.CompleteTripUseCase
import com.ontheroad.core.domain.usecase.EstimateDistanceUseCase
import com.ontheroad.core.domain.usecase.GetCurrentLocationUseCase
import com.ontheroad.core.domain.usecase.GetShiftSummaryUseCase
import com.ontheroad.core.domain.usecase.GetTripHistoryUseCase
import com.ontheroad.core.domain.usecase.SearchAddressUseCase
import com.ontheroad.core.domain.usecase.StartTripUseCase

class ViewModelFactory(
    private val application: OnTheRoadApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = application.database
        val tripRepository = TripRepositoryImpl(database.tripDao())
        val shiftRepository = ShiftRepositoryImpl(database.shiftDao())
        val sharedPreferences = application.getSharedPreferences(
            UserPreferencesRepositoryImpl.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val userPreferencesRepository = UserPreferencesRepositoryImpl(sharedPreferences)
        val locationRepository = LocationRepositoryImpl(application)

        return when {
            modelClass.isAssignableFrom(TrackerViewModel::class.java) -> {
                val startTripUseCase = StartTripUseCase(tripRepository)
                val completeTripUseCase = CompleteTripUseCase(tripRepository)
                val searchAddressUseCase = SearchAddressUseCase(locationRepository)
                val getCurrentLocationUseCase = GetCurrentLocationUseCase(locationRepository)
                val estimateDistanceUseCase = EstimateDistanceUseCase()
                val calculateDirectFareUseCase = CalculateDirectFareUseCase()

                TrackerViewModel(
                    tripRepository = tripRepository,
                    startTripUseCase = startTripUseCase,
                    completeTripUseCase = completeTripUseCase,
                    userPreferencesRepository = userPreferencesRepository,
                    calculateDirectFareUseCase = calculateDirectFareUseCase,
                    estimateDistanceUseCase = estimateDistanceUseCase,
                    searchAddressUseCase = searchAddressUseCase,
                    getCurrentLocationUseCase = getCurrentLocationUseCase
                ) as T
            }
            modelClass.isAssignableFrom(ShiftViewModel::class.java) -> {
                val getShiftSummaryUseCase = GetShiftSummaryUseCase(tripRepository, shiftRepository)
                ShiftViewModel(shiftRepository, getShiftSummaryUseCase) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                val getTripHistoryUseCase = GetTripHistoryUseCase(tripRepository)
                HistoryViewModel(getTripHistoryUseCase, tripRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(userPreferencesRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
