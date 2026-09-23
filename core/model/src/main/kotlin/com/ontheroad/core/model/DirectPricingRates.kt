package com.ontheroad.core.model

/**
 * Driver-configurable pricing rates for offline / on-the-spot direct trip quotation.
 * Pure Kotlin data model conforming to ARCH-001 (zero Android SDK dependencies).
 */
data class DirectPricingRates(
    val baseFareAmountCents: Long = 10_000_00L,
    val ratePerKmAmountCents: Long = 3_500_00L,
    val minimumFareAmountCents: Long = 15_000_00L,
    val includedBaseDistanceKm: Double = 0.0,
    val roadDetourMultiplier: Double = 1.25
) {
    val baseFareFormatted: Double
        get() = baseFareAmountCents / 100.0

    val ratePerKmFormatted: Double
        get() = ratePerKmAmountCents / 100.0

    val minimumFareFormatted: Double
        get() = minimumFareAmountCents / 100.0
}
