package com.ontheroad.core.domain.usecase

import com.ontheroad.core.model.DiscrepancyResult
import kotlin.math.abs
import kotlin.math.round

/**
 * UseCase to compute the discrepancy between actual driven distance
 * and the gig platform's quoted distance.
 * Pure Kotlin, zero Android dependencies (ARCH-001).
 */
class CalculateDiscrepancyUseCase {

    operator fun invoke(
        actualDistanceMeters: Double,
        quotedDistanceMeters: Double?
    ): DiscrepancyResult? {
        if (quotedDistanceMeters == null || quotedDistanceMeters <= 0.0) {
            return null
        }

        val differenceMeters = round(actualDistanceMeters - quotedDistanceMeters)
        val percentageDifference = if (quotedDistanceMeters > 0) {
            round(((actualDistanceMeters - quotedDistanceMeters) / quotedDistanceMeters) * 10000.0) / 100.0
        } else {
            0.0
        }

        // We consider a discrepancy significant if it exceeds 50 meters
        val hasDiscrepancy = abs(differenceMeters) >= 50.0

        return DiscrepancyResult(
            actualDistanceMeters = actualDistanceMeters,
            quotedDistanceMeters = quotedDistanceMeters,
            differenceMeters = differenceMeters,
            percentageDifference = percentageDifference,
            hasDiscrepancy = hasDiscrepancy
        )
    }
}
