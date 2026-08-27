package com.ontheroad.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val shiftId: String?,
    val platformId: String,
    val categoryId: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val startAddress: String,
    val endAddress: String?,
    val startLatitude: Double,
    val startLongitude: Double,
    val endLatitude: Double?,
    val endLongitude: Double?,
    val actualDistanceMeters: Double,
    val quotedDistanceMeters: Double?,
    val durationSeconds: Long,
    val platformFeeAmountCents: Long,
    val cashCollectedAmountCents: Long,
    val tipAmountCents: Long,
    val notes: String,
    val status: String
) {
    fun toDomain(): Trip = Trip(
        id = id,
        shiftId = shiftId,
        platformId = platformId,
        categoryId = categoryId,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        startAddress = startAddress,
        endAddress = endAddress,
        startLatitude = startLatitude,
        startLongitude = startLongitude,
        endLatitude = endLatitude,
        endLongitude = endLongitude,
        actualDistanceMeters = actualDistanceMeters,
        quotedDistanceMeters = quotedDistanceMeters,
        durationSeconds = durationSeconds,
        platformFeeAmountCents = platformFeeAmountCents,
        cashCollectedAmountCents = cashCollectedAmountCents,
        tipAmountCents = tipAmountCents,
        notes = notes,
        status = runCatching { TripStatus.valueOf(status) }.getOrDefault(TripStatus.COMPLETED)
    )

    companion object {
        fun fromDomain(trip: Trip): TripEntity = TripEntity(
            id = trip.id,
            shiftId = trip.shiftId,
            platformId = trip.platformId,
            categoryId = trip.categoryId,
            startTimeMillis = trip.startTimeMillis,
            endTimeMillis = trip.endTimeMillis,
            startAddress = trip.startAddress,
            endAddress = trip.endAddress,
            startLatitude = trip.startLatitude,
            startLongitude = trip.startLongitude,
            endLatitude = trip.endLatitude,
            endLongitude = trip.endLongitude,
            actualDistanceMeters = trip.actualDistanceMeters,
            quotedDistanceMeters = trip.quotedDistanceMeters,
            durationSeconds = trip.durationSeconds,
            platformFeeAmountCents = trip.platformFeeAmountCents,
            cashCollectedAmountCents = trip.cashCollectedAmountCents,
            tipAmountCents = trip.tipAmountCents,
            notes = trip.notes,
            status = trip.status.name
        )
    }
}
