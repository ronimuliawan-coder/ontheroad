package com.ontheroad.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ontheroad.core.model.RoutePoint

@Entity(
    tableName = "route_points",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tripId"])]
)
data class RoutePointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracyMeters: Float,
    val speedMps: Float,
    val timestampMillis: Long
) {
    fun toDomain(): RoutePoint = RoutePoint(
        id = id,
        tripId = tripId,
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracyMeters = accuracyMeters,
        speedMps = speedMps,
        timestampMillis = timestampMillis
    )

    companion object {
        fun fromDomain(point: RoutePoint): RoutePointEntity = RoutePointEntity(
            id = point.id,
            tripId = point.tripId,
            latitude = point.latitude,
            longitude = point.longitude,
            altitude = point.altitude,
            accuracyMeters = point.accuracyMeters,
            speedMps = point.speedMps,
            timestampMillis = point.timestampMillis
        )
    }
}
