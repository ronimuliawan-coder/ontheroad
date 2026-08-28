package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.ShiftRepository
import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.model.Expense
import com.ontheroad.core.model.ShiftSummary
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.max
import kotlin.math.round

/**
 * UseCase to calculate the consolidated shift financial and odometer reconciliation summary.
 * Pure Kotlin, zero Android dependencies (ARCH-001).
 */
class GetShiftSummaryUseCase(
    private val tripRepository: TripRepository,
    private val shiftRepository: ShiftRepository
) {

    suspend operator fun invoke(shiftId: String): ShiftSummary {
        val trips = tripRepository.getTripsByShiftId(shiftId).firstOrNull().orEmpty()
            .filter { it.status == TripStatus.COMPLETED }

        val expenses = shiftRepository.getExpensesForShift(shiftId).firstOrNull().orEmpty()

        return calculateSummary(shiftId, trips, expenses)
    }

    fun calculateSummary(
        shiftId: String?,
        trips: List<Trip>,
        expenses: List<Expense>
    ): ShiftSummary {
        val completedTrips = trips.filter { it.status == TripStatus.COMPLETED }

        var totalGrossEarningsCents = 0L
        var digitalBalanceCents = 0L
        var cashInHandCents = 0L
        var totalActualDistanceMeters = 0.0
        var totalQuotedDistanceMeters = 0.0
        var totalUncompensatedMeters = 0.0
        var totalDurationSeconds = 0L

        for (trip in completedTrips) {
            totalGrossEarningsCents += trip.totalEarningsCents
            digitalBalanceCents += trip.platformFeeAmountCents
            cashInHandCents += (trip.cashCollectedAmountCents + trip.tipAmountCents)
            totalActualDistanceMeters += trip.actualDistanceMeters
            totalDurationSeconds += trip.durationSeconds

            val quotedDistance = trip.quotedDistanceMeters
            if (quotedDistance != null && quotedDistance > 0.0) {
                totalQuotedDistanceMeters += quotedDistance
                val discrepancy = trip.actualDistanceMeters - quotedDistance
                if (discrepancy > 0.0) {
                    totalUncompensatedMeters += discrepancy
                }
            }
        }

        val totalExpensesCents = expenses.sumOf { it.amountCents }
        val netTakeHomeCents = totalGrossEarningsCents - totalExpensesCents

        val totalDistanceKm = totalActualDistanceMeters / 1000.0
        val totalHours = totalDurationSeconds / 3600.0

        val averageEarningsPerKmCents = if (totalDistanceKm > 0.0) {
            round((totalGrossEarningsCents / totalDistanceKm) * 100.0) / 100.0
        } else {
            0.0
        }

        val averageEarningsPerHourCents = if (totalHours > 0.0) {
            round((totalGrossEarningsCents / totalHours) * 100.0) / 100.0
        } else {
            0.0
        }

        return ShiftSummary(
            shiftId = shiftId,
            totalTrips = completedTrips.size,
            totalGrossEarningsCents = totalGrossEarningsCents,
            digitalBalanceCents = digitalBalanceCents,
            cashInHandCents = cashInHandCents,
            totalExpensesCents = totalExpensesCents,
            netTakeHomeCents = netTakeHomeCents,
            totalActualDistanceMeters = totalActualDistanceMeters,
            totalQuotedDistanceMeters = totalQuotedDistanceMeters,
            totalUncompensatedMeters = totalUncompensatedMeters,
            totalDurationSeconds = totalDurationSeconds,
            averageEarningsPerKmCents = averageEarningsPerKmCents,
            averageEarningsPerHourCents = averageEarningsPerHourCents
        )
    }
}
