package com.ontheroad.core.data.repository

import com.ontheroad.core.data.local.dao.TripDao
import com.ontheroad.core.data.local.entity.RoutePointEntity
import com.ontheroad.core.data.local.entity.TripEntity
import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.model.RoutePoint
import com.ontheroad.core.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepositoryImpl(
    private val tripDao: TripDao
) : TripRepository {

    override suspend fun insertTrip(trip: Trip) {
        tripDao.insertTrip(TripEntity.fromDomain(trip))
    }

    override suspend fun updateTrip(trip: Trip) {
        tripDao.updateTrip(TripEntity.fromDomain(trip))
    }

    override suspend fun getTripById(id: String): Trip? {
        return tripDao.getTripById(id)?.toDomain()
    }

    override fun getActiveTrip(): Flow<Trip?> {
        return tripDao.getActiveTrip().map { it?.toDomain() }
    }

    override fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTrips().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTripsByShiftId(shiftId: String): Flow<List<Trip>> {
        return tripDao.getTripsByShiftId(shiftId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertRoutePoint(routePoint: RoutePoint) {
        tripDao.insertRoutePoint(RoutePointEntity.fromDomain(routePoint))
    }

    override fun getRoutePointsForTrip(tripId: String): Flow<List<RoutePoint>> {
        return tripDao.getRoutePointsForTrip(tripId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteTrip(id: String) {
        tripDao.deleteTrip(id)
    }
}
