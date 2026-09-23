package com.ontheroad.receipt

import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus

data class DirectTripReceiptContent(
    val completedAtMillis: Long,
    val pickupAddress: String,
    val dropoffAddress: String,
    val actualDistanceMeters: Double,
    val customerPaidTotalAmountCents: Long
)

fun Trip.toDirectTripReceiptContent(): DirectTripReceiptContent? {
    if (platformId != DIRECT_PLATFORM_ID || status != TripStatus.COMPLETED) return null
    val paidTotal = customerPaidTotalAmountCents ?: return null
    val completedAt = endTimeMillis ?: return null
    if (paidTotal < 0L || !actualDistanceMeters.isFinite() || actualDistanceMeters < 0.0) return null

    return DirectTripReceiptContent(
        completedAtMillis = completedAt,
        pickupAddress = startAddress,
        dropoffAddress = endAddress.orEmpty(),
        actualDistanceMeters = actualDistanceMeters,
        customerPaidTotalAmountCents = paidTotal
    )
}

private const val DIRECT_PLATFORM_ID = "direct"
