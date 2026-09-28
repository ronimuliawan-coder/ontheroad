package com.ontheroad.receipt

import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Test

class DirectTripReceiptContentTest {

    @Test
    fun `completed direct trip produces a receipt whitelist`() {
        val trip = completedDirectTrip()

        val content = trip.toDirectTripReceiptContent()

        assertNotNull(content)
        assertEquals(2_000L, content?.completedAtMillis)
        assertEquals("Pickup", content?.pickupAddress)
        assertEquals("Drop-off", content?.dropoffAddress)
        assertEquals(4_200.0, content?.actualDistanceMeters ?: 0.0, 0.001)
        assertEquals(12_500L, content?.customerPaidTotalAmountCents)
        assertFalse(content?.toString()?.contains("private note") ?: true)
    }

    @Test
    fun `receipt requires completed direct trip and recorded nonnegative payment`() {
        assertNull(completedDirectTrip().copy(platformId = "grab").toDirectTripReceiptContent())
        assertNull(completedDirectTrip().copy(status = TripStatus.IN_PROGRESS).toDirectTripReceiptContent())
        assertNull(completedDirectTrip().copy(customerPaidTotalAmountCents = null).toDirectTripReceiptContent())
        assertNull(completedDirectTrip().copy(customerPaidTotalAmountCents = -1L).toDirectTripReceiptContent())
    }

    private fun completedDirectTrip() = Trip(
        id = "trip-1",
        platformId = "direct",
        categoryId = "passenger",
        startTimeMillis = 1_000L,
        endTimeMillis = 2_000L,
        startAddress = "Pickup",
        endAddress = "Drop-off",
        startLatitude = 1.0,
        startLongitude = 2.0,
        endLatitude = 3.0,
        endLongitude = 4.0,
        actualDistanceMeters = 4_200.0,
        customerPaidTotalAmountCents = 12_500L,
        notes = "private note",
        status = TripStatus.COMPLETED
    )
}
