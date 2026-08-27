package com.ontheroad.core.domain.usecase

import com.ontheroad.core.model.ProfitabilityMetrics
import kotlin.math.round

/**
 * UseCase to compute earnings efficiency metrics (earnings/km and earnings/hour).
 * Pure Kotlin, zero Android dependencies (ARCH-001).
 */
class CalculateProfitabilityUseCase {

    operator fun invoke(
        totalEarningsCents: Long,
        distanceMeters: Double,
        durationSeconds: Long
    ): ProfitabilityMetrics {
        val distanceKm = distanceMeters / 1000.0
        val durationHours = durationSeconds / 3600.0

        val earningsPerKmCents = if (distanceKm > 0.0) {
            round((totalEarningsCents / distanceKm) * 100.0) / 100.0
        } else {
            0.0
        }

        val earningsPerHourCents = if (durationHours > 0.0) {
            round((totalEarningsCents / durationHours) * 100.0) / 100.0
        } else {
            0.0
        }

        return ProfitabilityMetrics(
            totalEarningsCents = totalEarningsCents,
            distanceKm = distanceKm,
            durationHours = durationHours,
            earningsPerKmCents = earningsPerKmCents,
            earningsPerHourCents = earningsPerHourCents
        )
    }
}
