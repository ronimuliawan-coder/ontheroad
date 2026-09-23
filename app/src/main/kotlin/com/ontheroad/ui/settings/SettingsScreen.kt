package com.ontheroad.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ontheroad.R
import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.model.DirectPricingProfileSettings
import com.ontheroad.core.model.ThemeMode
import com.ontheroad.core.model.isPersistable
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.GreenProfit
import com.ontheroad.core.ui.theme.OnSurfaceSecondary
import com.ontheroad.core.ui.theme.OnTheRoadTheme
import com.ontheroad.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val directPricingProfiles by viewModel.directPricingProfiles.collectAsState()

    SettingsScreenContent(
        themeMode = themeMode,
        onThemeModeSelected = { viewModel.setThemeMode(it) },
        directPricingRates = directPricingProfiles.activeProfile.rates,
        onUpdateRates = { viewModel.updateDirectPricingRates(it) },
        directPricingProfiles = directPricingProfiles,
        onSelectProfile = viewModel::selectDirectPricingProfile,
        onCreateProfile = viewModel::createDirectPricingProfile,
        onDeleteProfile = viewModel::deleteDirectPricingProfile,
        modifier = modifier
    )
}

@Composable
fun SettingsScreenContent(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    directPricingRates: DirectPricingRates,
    onUpdateRates: (DirectPricingRates) -> Unit,
    directPricingProfiles: DirectPricingProfileSettings = DirectPricingProfileSettings(),
    onSelectProfile: (String) -> Unit = {},
    onCreateProfile: (String) -> Unit = {},
    onDeleteProfile: (String) -> Unit = {},
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

        DirectPricingProfilesCard(
            profiles = directPricingProfiles,
            onSelectProfile = onSelectProfile,
            onCreateProfile = onCreateProfile,
            onDeleteProfile = onDeleteProfile
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
private fun DirectPricingProfilesCard(
    profiles: DirectPricingProfileSettings,
    onSelectProfile: (String) -> Unit,
    onCreateProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var profileName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val activeProfile = profiles.activeProfile

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CockpitDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingSmall)
        ) {
            Text(
                text = stringResource(R.string.direct_profiles_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.direct_profiles_description),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingSmall)
            ) {
                profiles.profiles.forEach { profile ->
                    FilterChip(
                        selected = profile.id == profiles.activeProfileId,
                        onClick = { onSelectProfile(profile.id) },
                        label = { Text(profile.name) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingSmall)
            ) {
                Button(
                    onClick = {
                        profileName = ""
                        showCreateDialog = true
                    },
                    modifier = Modifier.weight(1f).testTag("direct_profile_add")
                ) {
                    Text(stringResource(R.string.direct_profile_add))
                }
                TextButton(
                    onClick = { showDeleteDialog = true },
                    enabled = profiles.profiles.size > 1,
                    modifier = Modifier.weight(1f).testTag("direct_profile_delete")
                ) {
                    Text(stringResource(R.string.direct_profile_delete))
                }
            }
        }
    }

    if (showCreateDialog) {
        val trimmedName = profileName.trim()
        val duplicateName = profiles.profiles.any { it.name.equals(trimmedName, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(stringResource(R.string.direct_profile_create_title)) },
            text = {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it.take(DirectPricingProfileSettings.MAX_PROFILE_NAME_LENGTH) },
                    label = { Text(stringResource(R.string.direct_profile_name)) },
                    singleLine = true,
                    isError = duplicateName,
                    supportingText = if (duplicateName) {
                        { Text(stringResource(R.string.direct_profile_name_duplicate)) }
                    } else null,
                    modifier = Modifier.testTag("direct_profile_name")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreateProfile(trimmedName)
                        showCreateDialog = false
                    },
                    enabled = trimmedName.isNotEmpty() && !duplicateName,
                    modifier = Modifier.testTag("direct_profile_create_confirm")
                ) {
                    Text(stringResource(R.string.direct_profile_create))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.direct_profile_delete_title)) },
            text = { Text(stringResource(R.string.direct_profile_delete_message, activeProfile.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile(activeProfile.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.direct_profile_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
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
    var detourFactorText by remember(rates) { mutableStateOf(rates.roadDetourMultiplier.toString()) }
    val updatedRates = parseRatesDraft(
        baseFareText = baseFareText,
        ratePerKmText = ratePerKmText,
        minFareText = minFareText,
        includedKmText = includedKmText,
        detourFactorText = detourFactorText,
        rates = rates
    )

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
                    onValueChange = { baseFareText = it },
                    label = { Text("Base Fare (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("direct_base_fare"),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
                    isError = parseAmountCents(baseFareText) == null
                )

                OutlinedTextField(
                    value = ratePerKmText,
                    onValueChange = { ratePerKmText = it },
                    label = { Text("Rate/Km (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
                    isError = parseAmountCents(ratePerKmText) == null
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CockpitDimens.SpacingSmall)
            ) {
                OutlinedTextField(
                    value = minFareText,
                    onValueChange = { minFareText = it },
                    label = { Text("Min Fare (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
                    isError = parseAmountCents(minFareText) == null
                )

                OutlinedTextField(
                    value = includedKmText,
                    onValueChange = { includedKmText = it },
                    label = { Text("Base Km Inc.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f).testTag("direct_included_distance"),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
                    isError = parseIncludedDistance(includedKmText) == null
                )
            }

            Text(
                text = stringResource(R.string.direct_rates_advanced),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = detourFactorText,
                onValueChange = { detourFactorText = it },
                label = { Text(stringResource(R.string.direct_road_detour_factor)) },
                supportingText = { Text(stringResource(R.string.direct_road_detour_help)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("direct_road_detour_factor"),
                singleLine = true,
                shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
                isError = parseDetourFactor(detourFactorText) == null
            )

            if (updatedRates == null) {
                Text(
                    text = stringResource(R.string.direct_rates_invalid),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { updatedRates?.let(onUpdateRates) },
                enabled = updatedRates != null && updatedRates != rates,
                modifier = Modifier.fillMaxWidth().testTag("direct_rates_save_button")
            ) {
                Text(stringResource(R.string.direct_rates_save))
            }
        }
    }
}

private fun parseRatesDraft(
    baseFareText: String,
    ratePerKmText: String,
    minFareText: String,
    includedKmText: String,
    detourFactorText: String,
    rates: DirectPricingRates
): DirectPricingRates? {
    val baseFareCents = parseAmountCents(baseFareText) ?: return null
    val ratePerKmCents = parseAmountCents(ratePerKmText) ?: return null
    val minimumFareCents = parseAmountCents(minFareText) ?: return null
    val includedBaseDistanceKm = parseIncludedDistance(includedKmText) ?: return null
    val roadDetourMultiplier = parseDetourFactor(detourFactorText) ?: return null

    return rates.copy(
        baseFareAmountCents = baseFareCents,
        ratePerKmAmountCents = ratePerKmCents,
        minimumFareAmountCents = minimumFareCents,
        includedBaseDistanceKm = includedBaseDistanceKm,
        roadDetourMultiplier = roadDetourMultiplier
    ).takeIf(DirectPricingRates::isPersistable)
}

private fun parseDetourFactor(text: String): Double? = text.toDoubleOrNull()?.takeIf {
    it.isFinite() && it >= 0.0 && it.toFloat().isFinite()
}

private fun parseAmountCents(text: String): Long? {
    val amount = text.toLongOrNull()?.takeIf { it >= 0L } ?: return null
    return try {
        Math.multiplyExact(amount, 100L)
    } catch (_: ArithmeticException) {
        null
    }
}

private fun parseIncludedDistance(text: String): Double? {
    val distanceKm = text.toDoubleOrNull() ?: return null
    return distanceKm.takeIf {
        it.isFinite() && it >= 0.0 && it.toFloat().isFinite()
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
