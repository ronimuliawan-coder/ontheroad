package com.ontheroad.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ontheroad.core.model.AddressSuggestion
import com.ontheroad.core.model.DirectPricingRates
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.DirectBlue
import com.ontheroad.core.ui.theme.GreenProfit
import com.ontheroad.core.ui.theme.OnSurfaceSecondary
import com.ontheroad.core.ui.theme.OnSurfaceWhite

/**
 * High-contrast Cockpit component for quoting and starting on-the-spot direct bookings.
 * Address lookup is best-effort; manual addresses and distance remain available as the fallback.
 * Follows UI-001 (Unidirectional Data Flow) and Cockpit Design Tokens.
 */
@Composable
fun DirectBookingCard(
    pickupAddress: String,
    onPickupAddressChange: (String) -> Unit,
    pickupSuggestions: List<AddressSuggestion> = emptyList(),
    onSelectPickupSuggestion: (AddressSuggestion) -> Unit = {},
    onAcquireCurrentLocation: () -> Unit,
    destinationAddress: String,
    onDestinationAddressChange: (String) -> Unit,
    destinationSuggestions: List<AddressSuggestion> = emptyList(),
    onSelectDestinationSuggestion: (AddressSuggestion) -> Unit = {},
    isSearchingAddress: Boolean = false,
    addressLookupHint: String? = null,
    estimatedDistanceKmText: String,
    onEstimatedDistanceChange: (String) -> Unit,
    isDistanceAutoCalculated: Boolean = false,
    estimatedFareCents: Long,
    rates: DirectPricingRates,
    customFareOverrideText: String,
    onCustomFareOverrideChange: (String) -> Unit,
    onStartDirectRun: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomFareInput by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CockpitDimens.SpacingMedium)
        ) {
            // Header: Direct Booking Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = DirectBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DIRECT QUOTE / MANUAL FALLBACK",
                        style = MaterialTheme.typography.labelMedium,
                        color = DirectBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                if (isSearchingAddress) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = DirectBlue,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Searching...",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceSecondary
                        )
                    }
                }
            }

            addressLookupHint?.let { hint ->
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceSecondary,
                    modifier = Modifier.padding(top = CockpitDimens.SpacingSmall)
                )
            }

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingMedium))

            // 1. Pickup Address Field with GPS Fix Button
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = onPickupAddressChange,
                    label = { Text("Pickup Location") },
                    placeholder = { Text("e.g. Current GPS Location or Street") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Pickup Location",
                            tint = DirectBlue
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = onAcquireCurrentLocation) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Use GPS Location",
                                tint = GreenProfit
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
                )

                // Pickup Suggestions Dropdown List
                if (pickupSuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(CockpitDimens.SpacingSmall)) {
                            pickupSuggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectPickupSuggestion(suggestion) }
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = DirectBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = suggestion.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = suggestion.fullAddress,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

            // 2. Destination Address Field with Auto Search Dropdown
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = destinationAddress,
                    onValueChange = onDestinationAddressChange,
                    label = { Text("Destination / Dropoff") },
                    placeholder = { Text("Type place, station, airport...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Destination",
                            tint = DirectBlue
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
                )

                // Destination Suggestions Dropdown List
                if (destinationSuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(CockpitDimens.SpacingSmall)) {
                            destinationSuggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectDestinationSuggestion(suggestion) }
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = GreenProfit,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = suggestion.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = suggestion.fullAddress,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

            // 3. Estimated Distance (km) with Auto-calculated Tag
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = estimatedDistanceKmText,
                    onValueChange = onEstimatedDistanceChange,
                    label = { Text("Estimated Distance (km)") },
                    placeholder = { Text("Auto-calculated or enter manually") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Icon(
                            imageVector = if (isDistanceAutoCalculated) Icons.Default.AutoAwesome else Icons.Default.Edit,
                            contentDescription = "Distance",
                            tint = if (isDistanceAutoCalculated) GreenProfit else OnSurfaceSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
                )

                if (isDistanceAutoCalculated && estimatedDistanceKmText.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Auto-calculated distance (1.30x road curvature detour applied)",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenProfit,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingMedium))

            // 4. Live Fare Quotation Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                            text = "ESTIMATED FARE",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.clickable {
                                showCustomFareInput = !showCustomFareInput
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Override Fare",
                                tint = DirectBlue,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = if (showCustomFareInput) "Use Auto Rate" else "Custom Price",
                                style = MaterialTheme.typography.labelSmall,
                                color = DirectBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val formattedFare = if (estimatedFareCents >= 100) {
                        String.format("%,d", estimatedFareCents / 100)
                    } else {
                        "$estimatedFareCents"
                    }

                    Text(
                        text = "Rp $formattedFare",
                        style = MaterialTheme.typography.headlineLarge,
                        color = GreenProfit,
                        fontWeight = FontWeight.ExtraBold
                    )

                    val rateSummary = "Base: Rp ${(rates.baseFareAmountCents / 100)} + Rp ${(rates.ratePerKmAmountCents / 100)}/km (Min: Rp ${(rates.minimumFareAmountCents / 100)})"
                    Text(
                        text = rateSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceSecondary
                    )

                    AnimatedVisibility(visible = showCustomFareInput) {
                        Column(modifier = Modifier.padding(top = CockpitDimens.SpacingSmall)) {
                            OutlinedTextField(
                                value = customFareOverrideText,
                                onValueChange = onCustomFareOverrideChange,
                                label = { Text("Manual Fare Override (Rp)") },
                                placeholder = { Text("e.g. 50000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingLarge))

            // 5. Start Direct Run Action Button (UI-001: 64dp height)
            CockpitButton(
                text = "Start Direct Run",
                onClick = onStartDirectRun,
                icon = Icons.Default.PlayArrow,
                containerColor = DirectBlue
            )
        }
    }
}
