package com.ontheroad.core.model

/**
 * Represents a single tracked trip from pickup to dropoff.
 * Pure Kotlin data model conforming to ARCH-001 (zero Android SDK dependencies).
 */
data class Trip(
    val id: String,
    val shiftId: String? = null,
    val platformId: String,
    val categoryId: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val startAddress: String,
    val endAddress: String? = null,
    val startLatitude: Double,
    val startLongitude: Double,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
    val actualDistanceMeters: Double = 0.0,
    val quotedDistanceMeters: Double? = null,
    val quotedFareAmountCents: Long? = null,
    val durationSeconds: Long = 0,
    val platformFeeAmountCents: Long = 0,
    val cashCollectedAmountCents: Long = 0,
    val tipAmountCents: Long = 0,
    val notes: String = "",
    val status: TripStatus = TripStatus.IN_PROGRESS,
    val customerPaidTotalAmountCents: Long? = null
) {
    val totalEarningsCents: Long
        get() = platformFeeAmountCents + cashCollectedAmountCents + tipAmountCents

    val actualDistanceKm: Double
        get() = actualDistanceMeters / 1000.0

    val quotedDistanceKm: Double?
        get() = quotedDistanceMeters?.let { it / 1000.0 }
}

enum class TripStatus {
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    CANCELLED
}

data class RoutePoint(
    val id: Long = 0,
    val tripId: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracyMeters: Float = 0f,
    val speedMps: Float = 0f,
    val timestampMillis: Long
)

data class Platform(
    val id: String,
    val name: String,
    val colorHex: String,
    val isCustom: Boolean = false
)

data class Category(
    val id: String,
    val name: String,
    val iconName: String = "default"
)

data class Shift(
    val id: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val dailyTargetCents: Long = 0,
    val status: ShiftStatus = ShiftStatus.ACTIVE
)

enum class ShiftStatus {
    ACTIVE,
    COMPLETED
}

data class Expense(
    val id: String,
    val shiftId: String? = null,
    val category: ExpenseCategory,
    val amountCents: Long,
    val timestampMillis: Long,
    val notes: String = ""
)

enum class ExpenseCategory {
    FUEL,
    CHARGING,
    TOLL,
    PARKING,
    WASH,
    MAINTENANCE,
    OTHER
}

/**
 * Result of comparing actual driven distance against platform quoted distance.
 */
data class DiscrepancyResult(
    val actualDistanceMeters: Double,
    val quotedDistanceMeters: Double,
    val differenceMeters: Double,
    val percentageDifference: Double,
    val hasDiscrepancy: Boolean
) {
    val differenceKm: Double
        get() = differenceMeters / 1000.0

    val isUndercompensated: Boolean
        get() = differenceMeters > 0
}

/**
 * Financial efficiency metrics for a trip or shift.
 */
data class ProfitabilityMetrics(
    val totalEarningsCents: Long,
    val distanceKm: Double,
    val durationHours: Double,
    val earningsPerKmCents: Double,
    val earningsPerHourCents: Double
)
