package com.ontheroad.core.domain.geo

import com.ontheroad.core.model.RoutePoint

/**
 * Filter to reject GPS bounce, stationary noise at traffic stops, and inaccurate satellite fixes.
 * Pure Kotlin, zero Android dependencies (ARCH-001).
 */
class GpsJitterFilter(
    private val maxAllowedAccuracyMeters: Float = 25.0f,
    private val minMovementThresholdMeters: Double = 4.0,
    private val minSpeedThresholdMps: Float = 0.5f,
    private val maxPlausibleSpeedMps: Double = 55.5 // ~200 km/h
) {

    /**
     * Determines whether a newly received route point should be accepted
     * for cumulative distance calculation.
     */
    fun shouldAcceptPoint(
        previousPoint: RoutePoint?,
        candidatePoint: RoutePoint
    ): Boolean {
        // 1. Reject inaccurate fixes (e.g., in tunnels or between skyscrapers)
        if (candidatePoint.accuracyMeters > maxAllowedAccuracyMeters) {
            return false
        }

        // First point is always accepted as origin anchor
        if (previousPoint == null) {
            return true
        }

        val distanceMeters = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = previousPoint.latitude,
            startLng = previousPoint.longitude,
            endLat = candidatePoint.latitude,
            endLng = candidatePoint.longitude
        )

        val timeDeltaSeconds = (candidatePoint.timestampMillis - previousPoint.timestampMillis) / 1000.0
        if (timeDeltaSeconds <= 0.0) {
            return false
        }

        // 2. Reject stationary jitter (traffic light / red signal drift)
        // If movement is tiny (< 4m) and candidate speed is below 0.5 m/s, it's stationary drift
        if (distanceMeters < minMovementThresholdMeters && candidatePoint.speedMps < minSpeedThresholdMps) {
            return false
        }

        // 3. Reject teleportation spikes (speed > 200 km/h)
        val calculatedSpeedMps = distanceMeters / timeDeltaSeconds
        if (calculatedSpeedMps > maxPlausibleSpeedMps) {
            return false
        }

        return true
    }
}
