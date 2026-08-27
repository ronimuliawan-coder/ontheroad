package com.ontheroad.core.domain.repository

import com.ontheroad.core.model.RoutePoint
import com.ontheroad.core.model.Trip
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining operations on Trip entities.
 * Pure Kotlin interface conforming to Clean Architecture and ARCH-001.
 */
interface TripRepository {
    suspend fun insertTrip(trip: Trip)
    suspend fun updateTrip(trip: Trip)
    suspend fun getTripById(id: String): Trip?
    fun getActiveTrip(): Flow<Trip?>
    fun getAllTrips(): Flow<List<Trip>>
    fun getTripsByShiftId(shiftId: String): Flow<List<Trip>>
    suspend fun insertRoutePoint(routePoint: RoutePoint)
    fun getRoutePointsForTrip(tripId: String): Flow<List<RoutePoint>>
    suspend fun deleteTrip(id: String)
}
