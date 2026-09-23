package com.ontheroad.core.domain.usecase

import com.ontheroad.core.model.DirectPricingRates
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateDirectFareUseCaseTest {

    private lateinit var useCase: CalculateDirectFareUseCase
    private val defaultRates = DirectPricingRates(
        baseFareAmountCents = 10_000_00L,       // Rp 10.000
        ratePerKmAmountCents = 3_500_00L,       // Rp 3.500/km
        minimumFareAmountCents = 15_000_00L,     // Rp 15.000 min fare
        includedBaseDistanceKm = 0.0,
        roadDetourMultiplier = 1.25
    )

    @Before
    fun setUp() {
        useCase = CalculateDirectFareUseCase()
    }

    @Test
    fun `calculates standard tiered fare correctly above minimum fare`() {
        // Distance: 5.0 km
        // Base: 10.000 + (5.0 * 3.500) = 10.000 + 17.500 = 27.500
        val fare = useCase(
            distanceKm = 5.0,
            rates = defaultRates
        )

        assertEquals(27_500_00L, fare)
    }

    @Test
    fun `enforces minimum fare threshold when calculated fare is lower`() {
        // Distance: 1.0 km
        // Base: 10.000 + (1.0 * 3.500) = 13.500 -> Less than minimum 15.000
        val fare = useCase(
            distanceKm = 1.0,
            rates = defaultRates
        )

        assertEquals(15_000_00L, fare)
    }

    @Test
    fun `respects included base distance before charging per km`() {
        val ratesWithBaseDistance = defaultRates.copy(
            baseFareAmountCents = 10_000_00L,
            ratePerKmAmountCents = 3_000_00L,
            minimumFareAmountCents = 10_000_00L,
            includedBaseDistanceKm = 2.0 // First 2km included
        )

        // Trip 1: 1.5 km (within base distance) -> 10.000
        val shortFare = useCase(distanceKm = 1.5, rates = ratesWithBaseDistance)
        assertEquals(10_000_00L, shortFare)

        // Trip 2: 5.0 km (2.0 km included, 3.0 km chargeable) -> 10.000 + (3.0 * 3.000) = 19.000
        val longFare = useCase(distanceKm = 5.0, rates = ratesWithBaseDistance)
        assertEquals(19_000_00L, longFare)
    }

    @Test
    fun `returns manual override fare when provided`() {
        val fare = useCase(
            distanceKm = 10.0,
            rates = defaultRates,
            customFareOverrideCents = 50_000_00L
        )

        assertEquals(50_000_00L, fare)
    }

    @Test
    fun `handles zero and negative distance safely`() {
        val zeroFare = useCase(distanceKm = 0.0, rates = defaultRates)
        assertEquals(15_000_00L, zeroFare) // Minimum fare applies

        val negFare = useCase(distanceKm = -5.0, rates = defaultRates)
        assertEquals(15_000_00L, negFare)
    }
}
