package com.ontheroad.core.domain.geo

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure Kotlin implementation of the Haversine formula to compute spherical
 * distance between two geographical points in meters.
 * Conforms to ARCH-001 (zero Android SDK dependencies).
 */
object HaversineDistanceCalculator {
    private const val EARTH_RADIUS_METERS = 6371000.0

    fun calculateDistanceMeters(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Double {
        if (startLat == endLat && startLng == endLng) return 0.0

        val dLat = Math.toRadians(endLat - startLat)
        val dLng = Math.toRadians(endLng - startLng)

        val startLatRad = Math.toRadians(startLat)
        val endLatRad = Math.toRadians(endLat)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(startLatRad) * cos(endLatRad) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }
}
