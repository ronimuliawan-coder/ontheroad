package com.ontheroad.ui.tracker

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ontheroad.core.ui.component.CockpitButton
import com.ontheroad.core.ui.component.DiscrepancyBadge
import com.ontheroad.core.ui.component.MetricCard
import com.ontheroad.core.ui.component.PlatformChip
import com.ontheroad.core.ui.component.RapidCompleteModal
import com.ontheroad.core.ui.theme.BrandEmerald
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.OnSurfaceSecondary
import com.ontheroad.core.ui.theme.OnTheRoadTheme
import com.ontheroad.core.ui.theme.RedDiscrepancy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    modifier: Modifier = Modifier
) {
    var isTracking by remember { mutableStateOf(false) }
    var selectedPlatform by remember { mutableStateOf("grab") }
    var actualDistanceMeters by remember { mutableDoubleStateOf(0.0) }
    var showCompleteModal by remember { mutableStateOf(false) }

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
                        isSelected = selectedPlatform == id,
                        onClick = { if (!isTracking) selectedPlatform = id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingLarge))

            // Hero Metric: Actual Odometer Distance
            MetricCard(
                title = "Actual Distance",
                value = String.format("%.2f", actualDistanceMeters / 1000.0),
                unit = "km",
                subtitle = if (isTracking) "GPS Breadcrumbs active" else "Ready to track run",
                badge = if (isTracking) {
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
                    value = if (isTracking) "00:14:32" else "--:--",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Speed",
                    value = if (isTracking) "38" else "0",
                    unit = "km/h",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Giant Cockpit Action Button (UI-001: 64dp height)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = CockpitDimens.SpacingLarge)
        ) {
            if (!isTracking) {
                CockpitButton(
                    text = "Start Trip",
                    onClick = {
                        isTracking = true
                        actualDistanceMeters = 3450.0 // Simulated active run distance
                    },
                    icon = Icons.Default.PlayArrow,
                    containerColor = BrandEmerald
                )
            } else {
                CockpitButton(
                    text = "Complete Trip",
                    onClick = { showCompleteModal = true },
                    icon = Icons.Default.Check,
                    containerColor = RedDiscrepancy
                )
            }
        }
    }

    if (showCompleteModal) {
        RapidCompleteModal(
            actualDistanceMeters = actualDistanceMeters,
            initialEndAddress = "Bundaran HI, Jakarta",
            onDismissRequest = { showCompleteModal = false },
            onCompleteTrip = { _, _, _, _, _ ->
                showCompleteModal = false
                isTracking = false
                actualDistanceMeters = 0.0
            }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun TrackerScreenPreview() {
    OnTheRoadTheme(darkTheme = true) {
        TrackerScreen()
    }
}
