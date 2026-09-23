package com.ontheroad.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ontheroad.core.model.ThemeMode
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.GreenProfit
import com.ontheroad.core.ui.theme.OnSurfaceSecondary
import com.ontheroad.core.ui.theme.OnTheRoadTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.ui.theme.DirectBlue
import com.ontheroad.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val directPricingRates by viewModel.directPricingRates.collectAsState()

    SettingsScreenContent(
        themeMode = themeMode,
        onThemeModeSelected = { viewModel.setThemeMode(it) },
        directPricingRates = directPricingRates,
        onUpdateRates = { viewModel.updateDirectPricingRates(it) },
        modifier = modifier
    )
}

@Composable
fun SettingsScreenContent(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    directPricingRates: DirectPricingRates,
    onUpdateRates: (DirectPricingRates) -> Unit,
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
            text = "COCKPIT & DISPLAY",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )

        ThemeSelectionCard(
            selectedMode = themeMode,
            onModeSelected = onThemeModeSelected
        )

        Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

        Text(
            text = "DIRECT BOOKING RATES (OFFLINE RUNS)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )

        DirectPricingRatesCard(
            rates = directPricingRates,
            onUpdateRates = onUpdateRates
        )

        Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

        Text(
            text = "DRIVER PREFERENCES",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )

        SettingItemCard(
            title = "Distance Unit",
            value = "Kilometers (km)",
            description = "Default unit for odometer and rate calculations"
        )

        SettingItemCard(
            title = "Currency Format",
            value = "Local Standard ($, Rp, €)",
            description = "Formatted based on driver locale"
        )

        SettingItemCard(
            title = "Privacy & Data Storage",
            value = "100% Offline & Private",
            description = "All GPS tracks and financial data remain exclusively on your device"
        )

        SettingItemCard(
            title = "Version",
            value = "OnTheRoad 0.2.0",
            description = "Open source cockpit tool built for gig workers"
        )
    }
}

@Composable
private fun DirectPricingRatesCard(
    rates: DirectPricingRates,
    onUpdateRates: (DirectPricingRates) -> Unit
) {
    var baseFareText by remember(rates) { mutableStateOf((rates.baseFareAmountCents / 100).toString()) }
    var ratePerKmText by remember(rates) { mutableStateOf((rates.ratePerKmAmountCents / 100).toString()) }
    var minFareText by remember(rates) { mutableStateOf((rates.minimumFareAmountCents / 100).toString()) }
    var includedKmText by remember(rates) { mutableStateOf(rates.includedBaseDistanceKm.toString()) }

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
                .padding(CockpitDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingSmall)
        ) {
            Text(
                text = "Fare Quotation Formula",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Used to auto-calculate price when quoting on-the-spot rides",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingSmall)
            ) {
                OutlinedTextField(
                    value = baseFareText,
                    onValueChange = {
                        baseFareText = it
                        val cents = (it.toLongOrNull() ?: 0L) * 100
                        onUpdateRates(rates.copy(baseFareAmountCents = cents))
                    },
                    label = { Text("Base Fare (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
                )

                OutlinedTextField(
                    value = ratePerKmText,
                    onValueChange = {
                        ratePerKmText = it
                        val cents = (it.toLongOrNull() ?: 0L) * 100
                        onUpdateRates(rates.copy(ratePerKmAmountCents = cents))
                    },
                    label = { Text("Rate/Km (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingSmall)
            ) {
                OutlinedTextField(
                    value = minFareText,
                    onValueChange = {
                        minFareText = it
                        val cents = (it.toLongOrNull() ?: 0L) * 100
                        onUpdateRates(rates.copy(minimumFareAmountCents = cents))
                    },
                    label = { Text("Min Fare (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
                )

                OutlinedTextField(
                    value = includedKmText,
                    onValueChange = {
                        includedKmText = it
                        val km = it.toDoubleOrNull() ?: 0.0
                        onUpdateRates(rates.copy(includedBaseDistanceKm = km))
                    },
                    label = { Text("Base Km Inc.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectionCard(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
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
            Text(
                text = "App Theme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose Cockpit OLED Dark for night driving or Light for direct sunlight",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceSecondary
            )
            Spacer(modifier = Modifier.height(CockpitDimens.SpacingMedium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionChip(
                    label = "System",
                    isSelected = selectedMode == ThemeMode.SYSTEM,
                    onClick = { onModeSelected(ThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionChip(
                    label = "Dark OLED",
                    isSelected = selectedMode == ThemeMode.DARK,
                    onClick = { onModeSelected(ThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionChip(
                    label = "Light",
                    isSelected = selectedMode == ThemeMode.LIGHT,
                    onClick = { onModeSelected(ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "themeChipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "themeChipText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CockpitDimens.ChipCornerRadius))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SettingItemCard(
    title: String,
    value: String,
    description: String
) {
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenProfit,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceSecondary
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun SettingsScreenPreview() {
    OnTheRoadTheme(themeMode = ThemeMode.DARK) {
        SettingsScreenContent(
            themeMode = ThemeMode.DARK,
            onThemeModeSelected = {},
            directPricingRates = DirectPricingRates(),
            onUpdateRates = {}
        )
    }
}
