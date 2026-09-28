package com.ontheroad.core.domain.usecase

import com.ontheroad.core.model.DirectPricingRates
import kotlin.math.roundToLong

/**
 * UseCase to calculate estimated fare for direct / offline trips based on
 * distance and driver pricing rates (Base Fare + Rate/km with Minimum Fare threshold).
 * Pure Kotlin, zero Android framework dependencies (ARCH-001).
 */
class CalculateDirectFareUseCase {

    /**
     * Computes quoted fare in cents.
     *
     * @param distanceKm Estimated trip distance in kilometers.
     * @param rates Driver's configured pricing rates.
     * @param customFareOverrideCents Optional manual driver override.
     * @return Final fare amount in cents.
     */
    operator fun invoke(
        distanceKm: Double,
        rates: DirectPricingRates,
        customFareOverrideCents: Long? = null
    ): Long {
        if (customFareOverrideCents != null && customFareOverrideCents > 0L) {
            return customFareOverrideCents
        }

        val safeDistanceKm = if (distanceKm.isNaN() || distanceKm < 0.0) 0.0 else distanceKm
        val includedBaseDistanceKm = rates.includedBaseDistanceKm
            .takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
        val chargeableKm = maxOf(0.0, safeDistanceKm - includedBaseDistanceKm)
        val ratePerKmAmountCents = rates.ratePerKmAmountCents.coerceAtLeast(0L)
        val rawDistanceChargeCents = chargeableKm * ratePerKmAmountCents.toDouble()
        val distanceChargeCents = when {
            rawDistanceChargeCents.isNaN() || rawDistanceChargeCents <= 0.0 -> 0L
            !rawDistanceChargeCents.isFinite() || rawDistanceChargeCents >= Long.MAX_VALUE.toDouble() -> Long.MAX_VALUE
            else -> rawDistanceChargeCents.roundToLong()
        }
        val baseFareAmountCents = rates.baseFareAmountCents.coerceAtLeast(0L)
        val calculatedFareCents = if (distanceChargeCents > Long.MAX_VALUE - baseFareAmountCents) {
            Long.MAX_VALUE
        } else {
            baseFareAmountCents + distanceChargeCents
        }

        return maxOf(rates.minimumFareAmountCents.coerceAtLeast(0L), calculatedFareCents)
    }
}
