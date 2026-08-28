package com.ontheroad.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ontheroad.core.data.local.entity.RoutePointEntity
import com.ontheroad.core.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: String): TripEntity?

    @Query("SELECT * FROM trips WHERE status = 'IN_PROGRESS' ORDER BY startTimeMillis DESC LIMIT 1")
    fun getActiveTrip(): Flow<TripEntity?>

    @Query("SELECT * FROM trips ORDER BY startTimeMillis DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("""
        SELECT * FROM trips 
        WHERE status = 'COMPLETED' 
        ORDER BY CASE WHEN actualDistanceMeters > 0 
            THEN ((platformFeeAmountCents + cashCollectedAmountCents + tipAmountCents) * 1.0) / actualDistanceMeters 
            ELSE 0.0 END DESC
    """)
    fun getAllTripsSortedByProfitability(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE shiftId = :shiftId ORDER BY startTimeMillis ASC")
    fun getTripsByShiftId(shiftId: String): Flow<List<TripEntity>>

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTrip(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePoint(point: RoutePointEntity)

    @Query("SELECT * FROM route_points WHERE tripId = :tripId ORDER BY timestampMillis ASC")
    fun getRoutePointsForTrip(tripId: String): Flow<List<RoutePointEntity>>
}
