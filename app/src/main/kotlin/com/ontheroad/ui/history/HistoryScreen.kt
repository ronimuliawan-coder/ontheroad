package com.ontheroad.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ontheroad.core.domain.usecase.TripSortOrder
import com.ontheroad.core.model.Trip
import com.ontheroad.core.ui.component.DiscrepancyBadge
import com.ontheroad.core.ui.component.PlatformChip
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.GreenProfit
import com.ontheroad.core.ui.theme.OnSurfaceSecondary
import com.ontheroad.viewmodel.HistoryUiState
import com.ontheroad.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryScreenContent(
        uiState = uiState,
        onSortChanged = viewModel::setSortOrder,
        modifier = modifier
    )
}

@Composable
fun HistoryScreenContent(
    uiState: HistoryUiState,
    onSortChanged: (TripSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortTabs = listOf(
        Pair("Recent", TripSortOrder.RECENT_FIRST),
        Pair("Most Profitable ($/km)", TripSortOrder.PROFITABILITY_HIGH_TO_LOW),
        Pair("Longest", TripSortOrder.DISTANCE_HIGH_TO_LOW)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = CockpitDimens.SpacingMedium)
    ) {
        Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

        // Sort Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingSmall)
        ) {
            sortTabs.forEach { (label, sortOrder) ->
                FilterChip(
                    selected = uiState.sortOrder == sortOrder,
                    onClick = { onSortChanged(sortOrder) },
                    label = { Text(label, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

        if (uiState.trips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(CockpitDimens.SpacingLarge),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No trips logged yet.\nComplete a run to see your discrepancy stats!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceSecondary
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingSmall)
            ) {
                items(uiState.trips, key = { it.id }) { trip ->
                    TripHistoryCard(trip)
                }
            }
        }
    }
}

@Composable
private fun TripHistoryCard(trip: Trip) {
    val ratePerKm = if (trip.actualDistanceKm > 0) {
        String.format("$%.2f/km", (trip.totalEarningsCents / 100.0) / trip.actualDistanceKm)
    } else {
        "$0.00/km"
    }

    val discrepancy = if (trip.quotedDistanceMeters != null) {
        trip.actualDistanceMeters - trip.quotedDistanceMeters!!
    } else {
        0.0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CockpitDimens.SpacingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatformChip(
                    name = trip.platformId.replaceFirstChar { it.uppercase() },
                    colorHex = "#00B14F",
                    isSelected = true,
                    onClick = {}
                )
                Text(
                    text = String.format("$%.2f", trip.totalEarningsCents / 100.0),
                    style = MaterialTheme.typography.titleLarge,
                    color = GreenProfit,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

            Text(
                text = "${trip.startAddress} → ${trip.endAddress ?: "Drop-off"}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingXSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format("%.2f km", trip.actualDistanceKm),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " • $ratePerKm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceSecondary
                    )
                }

                if (trip.quotedDistanceMeters != null) {
                    DiscrepancyBadge(differenceMeters = discrepancy)
                }
            }
        }
    }
}
