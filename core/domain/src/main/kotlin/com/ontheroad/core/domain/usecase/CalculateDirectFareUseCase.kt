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

        val safeDistanceKm = maxOf(0.0, distanceKm)
        val chargeableKm = maxOf(0.0, safeDistanceKm - rates.includedBaseDistanceKm)
        val distanceChargeCents = (chargeableKm * rates.ratePerKmAmountCents).roundToLong()
        val calculatedFareCents = rates.baseFareAmountCents + distanceChargeCents

        return maxOf(rates.minimumFareAmountCents, calculatedFareCents)
    }
}
