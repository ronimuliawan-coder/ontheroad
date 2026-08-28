package com.ontheroad.ui.shift

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ontheroad.core.ui.component.DiscrepancyBadge
import com.ontheroad.core.ui.component.MetricCard
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.GreenProfit
import com.ontheroad.viewmodel.ShiftUiState
import com.ontheroad.viewmodel.ShiftViewModel

@Composable
fun ShiftScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ShiftScreenContent(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
fun ShiftScreenContent(
    uiState: ShiftUiState,
    modifier: Modifier = Modifier
) {
    val summary = uiState.summary

    val totalGrossFormatted = summary?.let {
        String.format("$%.2f", it.totalGrossEarningsCents / 100.0)
    } ?: "$0.00"

    val digitalFormatted = summary?.let {
        String.format("$%.2f", it.digitalBalanceCents / 100.0)
    } ?: "$0.00"

    val cashFormatted = summary?.let {
        String.format("$%.2f", it.cashInHandCents / 100.0)
    } ?: "$0.00"

    val actualKmFormatted = summary?.let {
        String.format("%.1f", it.totalActualDistanceKm)
    } ?: "0.0"

    val quotedKmFormatted = summary?.let {
        String.format("%.1f", it.totalQuotedDistanceKm)
    } ?: "0.0"

    val rateKmFormatted = summary?.let {
        String.format("$%.2f", it.averageEarningsPerKmCents / 100.0)
    } ?: "$0.00"

    val rateHourFormatted = summary?.let {
        String.format("$%.2f", it.averageEarningsPerHourCents / 100.0)
    } ?: "$0.00"

    val tripsCount = summary?.totalTrips ?: 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = CockpitDimens.SpacingMedium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingMedium)
    ) {
        Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

        Text(
            text = "TODAY'S SHIFT RECONCILIATION",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )

        // 1. Gross Earnings Hero
        MetricCard(
            title = "Total Gross Revenue",
            value = totalGrossFormatted,
            subtitle = "$tripsCount completed trip(s)",
            valueColor = GreenProfit
        )

        // 2. Split: Digital vs Cash
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingMedium)
        ) {
            MetricCard(
                title = "App Balance",
                value = digitalFormatted,
                subtitle = "Digital payout",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Cash in Pocket",
                value = cashFormatted,
                subtitle = "Physical cash collected",
                modifier = Modifier.weight(1f)
            )
        }

        // 3. Odometer Reality Card
        MetricCard(
            title = "Odometer vs. Platform",
            value = actualKmFormatted,
            unit = "km",
            subtitle = "Platform quoted: $quotedKmFormatted km",
            badge = if (summary != null) {
                { DiscrepancyBadge(differenceMeters = summary.totalUncompensatedMeters) }
            } else null
        )

        // 4. Efficiency Rates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingMedium)
        ) {
            MetricCard(
                title = "Rate / km",
                value = rateKmFormatted,
                unit = "/km",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Rate / hour",
                value = rateHourFormatted,
                unit = "/hr",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(CockpitDimens.SpacingLarge))
    }
}
