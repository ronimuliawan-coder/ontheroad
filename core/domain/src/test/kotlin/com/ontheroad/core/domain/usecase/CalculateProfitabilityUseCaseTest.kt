package com.ontheroad.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateProfitabilityUseCaseTest {

    private lateinit var useCase: CalculateProfitabilityUseCase

    @Before
    fun setUp() {
        useCase = CalculateProfitabilityUseCase()
    }

    @Test
    fun calculatesEarningsPerKmAndPerHourAccurately() {
        // Earned $15.00 (1500 cents), drove 5.0 km (5000m) in 30 minutes (1800 seconds)
        // Rate: $3.00/km (300 cents/km), $30.00/hr (3000 cents/hr)
        val metrics = useCase(
            totalEarningsCents = 1500,
            distanceMeters = 5000.0,
            durationSeconds = 1800
        )

        assertEquals(1500, metrics.totalEarningsCents)
        assertEquals(5.0, metrics.distanceKm, 0.01)
        assertEquals(0.5, metrics.durationHours, 0.01)
        assertEquals(300.0, metrics.earningsPerKmCents, 0.01)
        assertEquals(3000.0, metrics.earningsPerHourCents, 0.01)
    }

    @Test
    fun handlesZeroDistanceAndDurationGracefully() {
        val metrics = useCase(
            totalEarningsCents = 1000,
            distanceMeters = 0.0,
            durationSeconds = 0
        )

        assertEquals(0.0, metrics.earningsPerKmCents, 0.01)
        assertEquals(0.0, metrics.earningsPerHourCents, 0.01)
    }
}
