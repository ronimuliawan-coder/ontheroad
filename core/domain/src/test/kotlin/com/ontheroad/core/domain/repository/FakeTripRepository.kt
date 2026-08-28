package com.ontheroad.core.domain.repository

import com.ontheroad.core.model.RoutePoint
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTripRepository : TripRepository {

    private val tripsFlow = MutableStateFlow<Map<String, Trip>>(emptyMap())
    private val routePointsFlow = MutableStateFlow<List<RoutePoint>>(emptyList())

    override suspend fun insertTrip(trip: Trip) {
        tripsFlow.value = tripsFlow.value + (trip.id to trip)
    }

    override suspend fun updateTrip(trip: Trip) {
        tripsFlow.value = tripsFlow.value + (trip.id to trip)
    }

    override suspend fun getTripById(id: String): Trip? {
        return tripsFlow.value[id]
    }

    override fun getActiveTrip(): Flow<Trip?> {
        return tripsFlow.map { map ->
            map.values.firstOrNull { it.status == TripStatus.IN_PROGRESS }
        }
    }

    override fun getAllTrips(): Flow<List<Trip>> {
        return tripsFlow.map { it.values.toList() }
    }

    override fun getTripsByShiftId(shiftId: String): Flow<List<Trip>> {
        return tripsFlow.map { map ->
            map.values.filter { it.shiftId == shiftId }
        }
    }

    override suspend fun insertRoutePoint(routePoint: RoutePoint) {
        routePointsFlow.value = routePointsFlow.value + routePoint
    }

    override fun getRoutePointsForTrip(tripId: String): Flow<List<RoutePoint>> {
        return routePointsFlow.map { list ->
            list.filter { it.tripId == tripId }
        }
    }

    override suspend fun deleteTrip(id: String) {
        tripsFlow.value = tripsFlow.value - id
    }
}
