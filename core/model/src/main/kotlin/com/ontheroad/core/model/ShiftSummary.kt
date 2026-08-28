package com.ontheroad.core.model

/**
 * Consolidated financial and odometer reconciliation summary for a driver shift.
 * Pure Kotlin data model conforming to ARCH-001.
 */
data class ShiftSummary(
    val shiftId: String?,
    val totalTrips: Int,
    val totalGrossEarningsCents: Long,
    val digitalBalanceCents: Long,
    val cashInHandCents: Long,
    val totalExpensesCents: Long,
    val netTakeHomeCents: Long,
    val totalActualDistanceMeters: Double,
    val totalQuotedDistanceMeters: Double,
    val totalUncompensatedMeters: Double,
    val totalDurationSeconds: Long,
    val averageEarningsPerKmCents: Double,
    val averageEarningsPerHourCents: Double
) {
    val totalActualDistanceKm: Double
        get() = totalActualDistanceMeters / 1000.0

    val totalQuotedDistanceKm: Double
        get() = totalQuotedDistanceMeters / 1000.0

    val totalUncompensatedKm: Double
        get() = totalUncompensatedMeters / 1000.0

    val totalDurationHours: Double
        get() = totalDurationSeconds / 3600.0
}
