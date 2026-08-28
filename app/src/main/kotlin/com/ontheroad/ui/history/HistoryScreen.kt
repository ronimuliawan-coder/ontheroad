package com.ontheroad.ui.history

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ontheroad.core.ui.component.DiscrepancyBadge
import com.ontheroad.core.ui.component.PlatformChip
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.GreenProfit
import com.ontheroad.core.ui.theme.OnSurfaceSecondary
import com.ontheroad.core.ui.theme.OnTheRoadTheme

private data class HistoryItemUi(
    val id: String,
    val platformName: String,
    val platformColor: String,
    val origin: String,
    val destination: String,
    val actualKm: Double,
    val discrepancyMeters: Double,
    val earningsFormatted: String,
    val ratePerKmFormatted: String,
    val timeAgo: String
)

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("Recent", "Most Profitable ($/km)", "Longest")

    val sampleTrips = remember {
        listOf(
            HistoryItemUi(
                id = "1",
                platformName = "Grab",
                platformColor = "#00B14F",
                origin = "Monas",
                destination = "Bundaran HI",
                actualKm = 4.8,
                discrepancyMeters = 600.0,
                earningsFormatted = "$18.50",
                ratePerKmFormatted = "$3.85/km",
                timeAgo = "35 mins ago"
            ),
            HistoryItemUi(
                id = "2",
                platformName = "Gojek",
                platformColor = "#00AA13",
                origin = "Grand Indonesia",
                destination = "Senayan City",
                actualKm = 5.2,
                discrepancyMeters = 0.0,
                earningsFormatted = "$16.00",
                ratePerKmFormatted = "$3.07/km",
                timeAgo = "1 hr ago"
            ),
            HistoryItemUi(
                id = "3",
                platformName = "ShopeeFood",
                platformColor = "#EE4D2D",
                origin = "Resto Padang",
                destination = "Apartemen Sudirman",
                actualKm = 2.1,
                discrepancyMeters = -200.0,
                earningsFormatted = "$8.00",
                ratePerKmFormatted = "$3.80/km",
                timeAgo = "2 hrs ago"
            )
        )
    }

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
            filterTabs.forEachIndexed { index, label ->
                FilterChip(
                    selected = selectedFilterIndex == index,
                    onClick = { selectedFilterIndex = index },
                    label = { Text(label, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingSmall)
        ) {
            items(sampleTrips, key = { it.id }) { item ->
                TripHistoryCard(item)
            }
        }
    }
}

@Composable
private fun TripHistoryCard(item: HistoryItemUi) {
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
                    name = item.platformName,
                    colorHex = item.platformColor,
                    isSelected = true,
                    onClick = {}
                )
                Text(
                    text = item.earningsFormatted,
                    style = MaterialTheme.typography.titleLarge,
                    color = GreenProfit,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

            Text(
                text = "${item.origin} → ${item.destination}",
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
                        text = "${item.actualKm} km",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " • ${item.ratePerKmFormatted}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceSecondary
                    )
                }

                DiscrepancyBadge(differenceMeters = item.discrepancyMeters)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun HistoryScreenPreview() {
    OnTheRoadTheme(darkTheme = true) {
        HistoryScreen()
    }
}
