package com.ontheroad

import android.app.Application
import android.content.Context
import com.ontheroad.core.data.local.OnTheRoadDatabase
import com.ontheroad.core.data.repository.LocationRepositoryImpl
import com.ontheroad.core.data.repository.ShiftRepositoryImpl
import com.ontheroad.core.data.repository.TripRepositoryImpl
import com.ontheroad.core.data.repository.UserPreferencesRepositoryImpl
import com.ontheroad.core.domain.repository.LocationRepository
import com.ontheroad.core.domain.repository.ShiftRepository
import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.domain.repository.UserPreferencesRepository

class OnTheRoadApplication : Application() {

    val database: OnTheRoadDatabase by lazy {
        OnTheRoadDatabase.getInstance(this)
    }

    // Single shared instances: repository Flows are in-memory, so every ViewModel must
    // observe the same object or cross-screen writes stay invisible until process restart.
    val tripRepository: TripRepository by lazy { TripRepositoryImpl(database.tripDao()) }
    val shiftRepository: ShiftRepository by lazy { ShiftRepositoryImpl(database.shiftDao()) }
    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(
            getSharedPreferences(UserPreferencesRepositoryImpl.PREFS_NAME, Context.MODE_PRIVATE)
        )
    }
    val locationRepository: LocationRepository by lazy { LocationRepositoryImpl(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
