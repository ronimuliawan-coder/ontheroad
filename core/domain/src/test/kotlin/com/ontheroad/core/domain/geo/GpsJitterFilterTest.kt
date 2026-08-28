package com.ontheroad.core.domain.geo

import com.ontheroad.core.model.RoutePoint
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GpsJitterFilterTest {

    private lateinit var filter: GpsJitterFilter

    @Before
    fun setUp() {
        filter = GpsJitterFilter(
            maxAllowedAccuracyMeters = 25.0f,
            minMovementThresholdMeters = 4.0,
            minSpeedThresholdMps = 0.5f,
            maxPlausibleSpeedMps = 55.5
        )
    }

    @Test
    fun acceptsFirstPointRegardlessOfMovement() {
        val firstPoint = RoutePoint(
            tripId = "trip-1",
            latitude = -6.175,
            longitude = 106.827,
            accuracyMeters = 10f,
            speedMps = 0f,
            timestampMillis = 1000
        )
        assertTrue(filter.shouldAcceptPoint(null, firstPoint))
    }

    @Test
    fun rejectsInaccurateLocationFixes() {
        val prev = RoutePoint(
            tripId = "trip-1",
            latitude = -6.175,
            longitude = 106.827,
            accuracyMeters = 10f,
            speedMps = 5f,
            timestampMillis = 1000
        )
        val inaccurate = RoutePoint(
            tripId = "trip-1",
            latitude = -6.176,
            longitude = 106.828,
            accuracyMeters = 35f, // > 25m allowed
            speedMps = 5f,
            timestampMillis = 2000
        )
        assertFalse(filter.shouldAcceptPoint(prev, inaccurate))
    }

    @Test
    fun rejectsStationaryJitterAtRedLight() {
        // Point drifts 2 meters in 1 second while stationary (speed 0.1 m/s)
        val prev = RoutePoint(
            tripId = "trip-1",
            latitude = -6.175000,
            longitude = 106.827000,
            accuracyMeters = 8f,
            speedMps = 0f,
            timestampMillis = 1000
        )
        val stationaryDrift = RoutePoint(
            tripId = "trip-1",
            latitude = -6.175015, // ~1.6m movement
            longitude = 106.827000,
            accuracyMeters = 8f,
            speedMps = 0.1f, // < 0.5 m/s
            timestampMillis = 2000
        )
        assertFalse(filter.shouldAcceptPoint(prev, stationaryDrift))
    }

    @Test
    fun acceptsLegitimateVehicleMovement() {
        // Driving down the road: 15 meters in 1 second (15 m/s = 54 km/h)
        val prev = RoutePoint(
            tripId = "trip-1",
            latitude = -6.175000,
            longitude = 106.827000,
            accuracyMeters = 8f,
            speedMps = 15f,
            timestampMillis = 1000
        )
        val drivingPoint = RoutePoint(
            tripId = "trip-1",
            latitude = -6.175135, // ~15m movement
            longitude = 106.827000,
            accuracyMeters = 8f,
            speedMps = 15f,
            timestampMillis = 2000
        )
        assertTrue(filter.shouldAcceptPoint(prev, drivingPoint))
    }

    @Test
    fun rejectsTeleportationSpike() {
        // Glitch: moved 500 meters in 1 second (500 m/s = 1800 km/h)
        val prev = RoutePoint(
            tripId = "trip-1",
            latitude = -6.175000,
            longitude = 106.827000,
            accuracyMeters = 8f,
            speedMps = 10f,
            timestampMillis = 1000
        )
        val teleportGlitch = RoutePoint(
            tripId = "trip-1",
            latitude = -6.179500, // ~500m movement
            longitude = 106.827000,
            accuracyMeters = 8f,
            speedMps = 10f,
            timestampMillis = 2000
        )
        assertFalse(filter.shouldAcceptPoint(prev, teleportGlitch))
    }
}
