package com.ontheroad.core.domain.geo

import org.junit.Assert.assertEquals
import org.junit.Test

class HaversineDistanceCalculatorTest {

    @Test
    fun returnsZeroForIdenticalCoordinates() {
        val distance = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = -6.175392,
            startLng = 106.827153,
            endLat = -6.175392,
            endLng = 106.827153
        )
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun calculatesDistanceBetweenRealWorldLandmarksAccurately() {
        // Jakarta Monas (-6.175392, 106.827153) to Bundaran HI (-6.195000, 106.823056)
        // Known straight-line distance: ~2,230 meters (+/- 20m)
        val distance = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = -6.175392,
            startLng = 106.827153,
            endLat = -6.195000,
            endLng = 106.823056
        )

        assertEquals(2230.0, distance, 50.0)
    }

    @Test
    fun isSymmetric() {
        val d1 = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = -6.175392,
            startLng = 106.827153,
            endLat = -6.195000,
            endLng = 106.823056
        )
        val d2 = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = -6.195000,
            startLng = 106.823056,
            endLat = -6.175392,
            endLng = 106.827153
        )
        assertEquals(d1, d2, 0.0001)
    }
}
