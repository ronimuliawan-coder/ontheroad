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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ontheroad.core.ui.component.DiscrepancyBadge
import com.ontheroad.core.ui.component.MetricCard
import com.ontheroad.core.ui.theme.BrandEmerald
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.GreenProfit
import com.ontheroad.core.ui.theme.OnTheRoadTheme

@Composable
fun ShiftScreen(
    modifier: Modifier = Modifier
) {
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
            value = "$142.50",
            subtitle = "7 completed trips across 2 platforms",
            valueColor = GreenProfit
        )

        // 2. Split: Digital vs Cash
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingMedium)
        ) {
            MetricCard(
                title = "App Balance",
                value = "$110.00",
                subtitle = "Digital transfer",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Cash in Pocket",
                value = "$32.50",
                subtitle = "Physical cash collected",
                modifier = Modifier.weight(1f)
            )
        }

        // 3. Odometer Reality Card
        MetricCard(
            title = "Odometer vs. Platform",
            value = "64.2",
            unit = "km",
            subtitle = "Platform quoted: 58.0 km",
            badge = { DiscrepancyBadge(differenceMeters = 6200.0) }
        )

        // 4. Efficiency Rates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingMedium)
        ) {
            MetricCard(
                title = "Rate / km",
                value = "$2.22",
                unit = "/km",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Rate / hour",
                value = "$28.50",
                unit = "/hr",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(CockpitDimens.SpacingLarge))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun ShiftScreenPreview() {
    OnTheRoadTheme(darkTheme = true) {
        ShiftScreen()
    }
}
