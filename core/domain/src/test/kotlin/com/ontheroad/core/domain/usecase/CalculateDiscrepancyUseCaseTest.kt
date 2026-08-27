package com.ontheroad.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateDiscrepancyUseCaseTest {

    private lateinit var useCase: CalculateDiscrepancyUseCase

    @Before
    fun setUp() {
        useCase = CalculateDiscrepancyUseCase()
    }

    @Test
    fun returnsNullWhenQuotedDistanceIsNull() {
        val result = useCase(actualDistanceMeters = 5000.0, quotedDistanceMeters = null)
        assertNull(result)
    }

    @Test
    fun returnsNullWhenQuotedDistanceIsZeroOrNegative() {
        val resultZero = useCase(actualDistanceMeters = 5000.0, quotedDistanceMeters = 0.0)
        assertNull(resultZero)

        val resultNegative = useCase(actualDistanceMeters = 5000.0, quotedDistanceMeters = -100.0)
        assertNull(resultNegative)
    }

    @Test
    fun calculatesPositiveDiscrepancyAccurately() {
        // Driver drove 4800m, platform quoted 4200m -> +600m discrepancy (14.29%)
        val result = useCase(actualDistanceMeters = 4800.0, quotedDistanceMeters = 4200.0)
        assertNotNull(result)
        result!!

        assertEquals(600.0, result.differenceMeters, 0.01)
        assertEquals(0.6, result.differenceKm, 0.01)
        assertEquals(14.29, result.percentageDifference, 0.01)
        assertTrue(result.hasDiscrepancy)
        assertTrue(result.isUndercompensated)
    }

    @Test
    fun ignoresTrivialDiscrepancyUnderFiftyMeters() {
        // Driver drove 4030m, platform quoted 4000m -> +30m difference (< 50m threshold)
        val result = useCase(actualDistanceMeters = 4030.0, quotedDistanceMeters = 4000.0)
        assertNotNull(result)
        result!!

        assertEquals(30.0, result.differenceMeters, 0.01)
        assertFalse(result.hasDiscrepancy)
    }

    @Test
    fun handlesDriverTakingShorterRoute() {
        // Driver drove 3800m, platform quoted 4200m -> -400m difference
        val result = useCase(actualDistanceMeters = 3800.0, quotedDistanceMeters = 4200.0)
        assertNotNull(result)
        result!!

        assertEquals(-400.0, result.differenceMeters, 0.01)
        assertEquals(-0.4, result.differenceKm, 0.01)
        assertTrue(result.hasDiscrepancy)
        assertFalse(result.isUndercompensated)
    }
}
