package com.ontheroad.ui.history

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ontheroad.core.domain.usecase.TripSortOrder
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus
import com.ontheroad.core.model.ThemeMode
import com.ontheroad.core.ui.theme.OnTheRoadTheme
import com.ontheroad.viewmodel.HistoryUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryReceiptTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun onlyCompletedDirectTripsExposeTheShareReceiptAction() {
        val directTrip = trip("direct-trip", "direct")
        var sharedTripId: String? = null

        composeRule.setContent {
            OnTheRoadTheme(themeMode = ThemeMode.SYSTEM) {
                HistoryScreenContent(
                    uiState = HistoryUiState(
                        trips = listOf(
                            directTrip,
                            trip("platform-trip", "grab"),
                            trip("in-progress-trip", "direct").copy(status = TripStatus.IN_PROGRESS)
                        ),
                        sortOrder = TripSortOrder.RECENT_FIRST
                    ),
                    onSortChanged = {},
                    onShareReceipt = { sharedTripId = it.id }
                )
            }
        }

        composeRule.onNodeWithTag("share_direct_receipt_direct-trip").performClick()
        composeRule.onNodeWithTag("share_direct_receipt_platform-trip").assertDoesNotExist()
        composeRule.onNodeWithTag("share_direct_receipt_in-progress-trip").assertDoesNotExist()
        composeRule.runOnIdle { assertEquals("direct-trip", sharedTripId) }
    }

    private fun trip(id: String, platformId: String) = Trip(
        id = id,
        platformId = platformId,
        categoryId = "passenger",
        startTimeMillis = 1_000L,
        endTimeMillis = 2_000L,
        startAddress = "Pickup",
        endAddress = "Drop-off",
        startLatitude = 1.0,
        startLongitude = 2.0,
        actualDistanceMeters = 4_000.0,
        customerPaidTotalAmountCents = if (platformId == "direct") 10_000L else null,
        status = TripStatus.COMPLETED
    )
}
