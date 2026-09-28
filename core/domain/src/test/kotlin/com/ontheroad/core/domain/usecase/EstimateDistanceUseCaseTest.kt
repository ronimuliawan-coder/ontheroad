package com.ontheroad.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EstimateDistanceUseCaseTest {

    private lateinit var useCase: EstimateDistanceUseCase

    @Before
    fun setUp() {
        useCase = EstimateDistanceUseCase()
    }

    @Test
    fun `estimates distance with detour multiplier correctly`() {
        // Jakarta Monas to Bundaran HI (~2.5km straight line)
        val monasLat = -6.175392
        val monasLng = 106.827153
        val hiLat = -6.195000
        val hiLng = 106.823056

        val straightLineDistance = useCase(
            startLat = monasLat,
            startLng = monasLng,
            endLat = hiLat,
            endLng = hiLng,
            roadDetourMultiplier = 1.0
        )

        val roadDistanceWithDetour = useCase(
            startLat = monasLat,
            startLng = monasLng,
            endLat = hiLat,
            endLng = hiLng,
            roadDetourMultiplier = 1.25
        )

        assertTrue(straightLineDistance > 2000.0 && straightLineDistance < 2500.0)
        assertEquals(straightLineDistance * 1.25, roadDistanceWithDetour, 0.01)
    }

    @Test
    fun `returns zero distance for identical start and end coordinates`() {
        val distance = useCase(
            startLat = -6.175392,
            startLng = 106.827153,
            endLat = -6.175392,
            endLng = 106.827153,
            roadDetourMultiplier = 1.25
        )

        assertEquals(0.0, distance, 0.001)
    }
}
