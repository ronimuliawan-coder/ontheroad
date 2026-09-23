package com.ontheroad.ui.tracker

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ontheroad.core.ui.component.CockpitButton
import com.ontheroad.core.ui.component.DiscrepancyBadge
import com.ontheroad.core.ui.component.MetricCard
import com.ontheroad.core.ui.component.PlatformChip
import com.ontheroad.core.ui.component.RapidCompleteModal
import com.ontheroad.core.ui.theme.BrandEmerald
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.OnSurfaceSecondary
import com.ontheroad.core.ui.theme.RedDiscrepancy
import com.ontheroad.core.ui.component.DirectBookingCard
import com.ontheroad.service.LocationTrackingService
import com.ontheroad.viewmodel.TrackerUiState
import com.ontheroad.viewmodel.TrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    TrackerScreenContent(
        uiState = uiState,
        onSelectPlatform = viewModel::selectPlatform,
        onStartTrip = {
            viewModel.startTrip(
                startAddress = "Current GPS Location",
                startLatitude = -6.175392,
                startLongitude = 106.827153,
                onSuccess = { trip ->
                    LocationTrackingService.startTracking(context, trip.id)
                }
            )
        },
        onStartDirectTrip = {
            viewModel.startDirectTrip(
                startAddress = uiState.directPickupAddress,
                startLatitude = uiState.directPickupLatitude,
                startLongitude = uiState.directPickupLongitude,
                onSuccess = { trip ->
                    LocationTrackingService.startTracking(context, trip.id)
                }
            )
        },
        onPickupAddressChange = viewModel::updateDirectPickupAddress,
        onSelectPickupSuggestion = viewModel::selectPickupSuggestion,
        onAcquireCurrentLocation = viewModel::acquireCurrentLocation,
        onDestinationAddressChange = viewModel::updateDirectDestinationAddress,
        onSelectDestinationSuggestion = viewModel::selectDestinationSuggestion,
        onEstimatedDistanceChange = viewModel::updateDirectEstimatedDistance,
        onCustomFareOverrideChange = viewModel::updateDirectCustomFareOverride,
        onOpenCompleteModal = viewModel::openCompleteModal,
        onDismissCompleteModal = viewModel::dismissCompleteModal,
        onCompleteTrip = { endAddress, platformFee, cash, quotedDist, notes ->
            viewModel.completeTrip(
                endAddress = endAddress,
                endLatitude = -6.195000,
                endLongitude = 106.823056,
                platformFeeAmountCents = platformFee,
                cashCollectedAmountCents = cash,
                quotedDistanceMeters = quotedDist,
                notes = notes,
                onCompleted = {
                    LocationTrackingService.stopTracking(context)
                }
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreenContent(
    uiState: TrackerUiState,
    onSelectPlatform: (String) -> Unit,
    onStartTrip: () -> Unit,
    onStartDirectTrip: () -> Unit = onStartTrip,
    onPickupAddressChange: (String) -> Unit = {},
    onSelectPickupSuggestion: (com.ontheroad.core.model.AddressSuggestion) -> Unit = {},
    onAcquireCurrentLocation: () -> Unit = {},
    onDestinationAddressChange: (String) -> Unit = {},
    onSelectDestinationSuggestion: (com.ontheroad.core.model.AddressSuggestion) -> Unit = {},
    onEstimatedDistanceChange: (String) -> Unit = {},
    onCustomFareOverrideChange: (String) -> Unit = {},
    onOpenCompleteModal: () -> Unit,
    onDismissCompleteModal: () -> Unit,
    onCompleteTrip: (String, Long, Long, Double?, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val platforms = remember {
        listOf(
            Triple("grab", "Grab", "#00B14F"),
            Triple("gojek", "Gojek", "#00AA13"),
            Triple("uber", "Uber", "#1E293B"),
            Triple("lyft", "Lyft", "#FF00BF"),
            Triple("shopeefood", "ShopeeFood", "#EE4D2D"),
            Triple("direct", "Direct", "#3B82F6")
        )
    }

    val hours = uiState.durationSeconds / 3600
    val minutes = (uiState.durationSeconds % 3600) / 60
    val seconds = uiState.durationSeconds % 60
    val formattedDuration = if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = CockpitDimens.SpacingMedium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(CockpitDimens.SpacingMedium))

            // Platform Selector Horizontal Scroll
            Text(
                text = "PLATFORM",
                style = MaterialTheme.typography.labelLarge,
                color = OnSurfaceSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingSmall)
            ) {
                platforms.forEach { (id, name, color) ->
                    PlatformChip(
                        name = name,
                        colorHex = color,
                        isSelected = uiState.selectedPlatformId == id,
                        onClick = { onSelectPlatform(id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingLarge))

            if (!uiState.isTracking && uiState.selectedPlatformId == "direct") {
                // Dedicated Direct / Offline Booking Quoting Card
                DirectBookingCard(
                    pickupAddress = uiState.directPickupAddress,
                    onPickupAddressChange = onPickupAddressChange,
                    pickupSuggestions = uiState.directPickupSuggestions,
                    onSelectPickupSuggestion = onSelectPickupSuggestion,
                    onAcquireCurrentLocation = onAcquireCurrentLocation,
                    destinationAddress = uiState.directDestinationAddress,
                    onDestinationAddressChange = onDestinationAddressChange,
                    destinationSuggestions = uiState.directDestinationSuggestions,
                    onSelectDestinationSuggestion = onSelectDestinationSuggestion,
                    isSearchingAddress = uiState.isSearchingAddress,
                    estimatedDistanceKmText = uiState.directEstimatedDistanceKmText,
                    onEstimatedDistanceChange = onEstimatedDistanceChange,
                    isDistanceAutoCalculated = uiState.isDistanceAutoCalculated,
                    estimatedFareCents = uiState.directCalculatedFareCents,
                    rates = uiState.directPricingRates,
                    customFareOverrideText = uiState.directCustomFareOverrideText,
                    onCustomFareOverrideChange = onCustomFareOverrideChange,
                    onStartDirectRun = onStartDirectTrip
                )
            } else {
                // Hero Metric: Actual Odometer Distance
                MetricCard(
                    title = "Actual Distance",
                    value = String.format("%.2f", uiState.actualDistanceKm),
                    unit = "km",
                    subtitle = if (uiState.isTracking) "GPS Breadcrumbs active" else "Ready to track run",
                    badge = if (uiState.isTracking) {
                        { DiscrepancyBadge(differenceMeters = 0.0) }
                    } else null
                )

                Spacer(modifier = Modifier.height(CockpitDimens.SpacingMedium))

                // Secondary Metrics: Duration & Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingMedium)
                ) {
                    MetricCard(
                        title = "Duration",
                        value = if (uiState.isTracking) formattedDuration else "--:--",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Speed",
                        value = if (uiState.isTracking) "${uiState.speedKmh.toInt()}" else "0",
                        unit = "km/h",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Giant Cockpit Action Button (when not in direct pre-trip quote card or when tracking)
        if (uiState.isTracking || uiState.selectedPlatformId != "direct") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = CockpitDimens.SpacingLarge)
            ) {
                if (!uiState.isTracking) {
                    CockpitButton(
                        text = "Start Trip",
                        onClick = onStartTrip,
                        icon = Icons.Default.PlayArrow,
                        containerColor = BrandEmerald
                    )
                } else {
                    CockpitButton(
                        text = "Complete Trip",
                        onClick = onOpenCompleteModal,
                        icon = Icons.Default.Check,
                        containerColor = RedDiscrepancy
                    )
                }
            }
        }
    }

    if (uiState.showCompleteModal) {
        val activeTrip = uiState.activeTrip
        val isDirectTrip = activeTrip?.platformId == "direct"
        RapidCompleteModal(
            actualDistanceMeters = uiState.actualDistanceKm * 1000.0,
            initialEndAddress = activeTrip?.endAddress ?: "Current Destination",
            initialPlatformFeeCents = if (isDirectTrip) {
                activeTrip?.quotedFareAmountCents ?: 0L
            } else {
                activeTrip?.platformFeeAmountCents ?: 0L
            },
            initialCashCollectedCents = activeTrip?.cashCollectedAmountCents ?: 0L,
            initialQuotedDistanceMeters = activeTrip?.quotedDistanceMeters,
            isDirectTrip = isDirectTrip,
            onDismissRequest = onDismissCompleteModal,
            onCompleteTrip = onCompleteTrip
        )
    }
}
