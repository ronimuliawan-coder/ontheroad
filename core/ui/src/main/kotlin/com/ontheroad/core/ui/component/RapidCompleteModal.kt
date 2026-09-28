package com.ontheroad.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.GreenProfit
import com.ontheroad.core.ui.theme.OnSurfaceSecondary
import com.ontheroad.core.ui.R
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Rapid trip completion modal designed for sub-30-second driver interaction (UI-002).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RapidCompleteModal(
    actualDistanceMeters: Double,
    initialEndAddress: String,
    initialPlatformFeeCents: Long = 0L,
    initialCashCollectedCents: Long = 0L,
    initialQuotedDistanceMeters: Double? = null,
    isDirectTrip: Boolean = false,
    onDismissRequest: () -> Unit,
    onCompleteTrip: (
        endAddress: String,
        platformFeeCents: Long,
        cashCollectedCents: Long,
        quotedDistanceMeters: Double?,
        notes: String
    ) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var endAddress by remember(initialEndAddress) { mutableStateOf(initialEndAddress) }
    var platformFeeText by remember(initialPlatformFeeCents) {
        mutableStateOf(centsToInputText(initialPlatformFeeCents))
    }
    var hasCash by remember { mutableStateOf(false) }
    var cashCollectedText by remember(initialCashCollectedCents) {
        mutableStateOf(centsToInputText(initialCashCollectedCents))
    }
    var quotedDistanceKmText by remember(initialQuotedDistanceMeters) {
        mutableStateOf(metersToInputText(initialQuotedDistanceMeters))
    }
    var notesText by remember { mutableStateOf("") }

    val quotedDistanceMeters by remember {
        derivedStateOf {
            quotedDistanceKmText.toDoubleOrNull()?.let { it * 1000.0 }
        }
    }

    val customerPaidTotalCents by remember(platformFeeText, cashCollectedText, hasCash, isDirectTrip) {
        derivedStateOf {
            if (isDirectTrip) {
                directCustomerPaidTotalCents(platformFeeText, cashCollectedText, hasCash)
            } else null
        }
    }

    val discrepancyMeters by remember {
        derivedStateOf {
            val quoted = quotedDistanceMeters
            if (quoted != null && quoted > 0) {
                actualDistanceMeters - quoted
            } else {
                null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(
            topStart = CockpitDimens.CardCornerRadius,
            topEnd = CockpitDimens.CardCornerRadius
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CockpitDimens.SpacingLarge)
                .padding(bottom = CockpitDimens.SpacingXXLarge)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Complete Trip",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingMedium))

            // 1. Drop-off Address (auto-populated from GPS)
            OutlinedTextField(
                value = endAddress,
                onValueChange = { endAddress = it },
                label = { Text("Drop-off Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
            )

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

            // 2. Earnings from Ride Sharing App
            OutlinedTextField(
                value = platformFeeText,
                onValueChange = { platformFeeText = it },
                label = { Text(if (isDirectTrip) "Direct Transfer Amount" else "App Payout / Platform Fee") },
                placeholder = { Text("e.g. 25000 or 15.00") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
            )

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))

            // 3. Cash-in-pocket toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customer paid in cash",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = hasCash,
                    onCheckedChange = { hasCash = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = GreenProfit)
                )
            }

            if (hasCash) {
                OutlinedTextField(
                    value = cashCollectedText,
                    onValueChange = { cashCollectedText = it },
                    label = { Text("Cash Amount Collected") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
                )
                Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))
            }

            if (isDirectTrip) {
                val paidTotal = customerPaidTotalCents
                if (paidTotal == null) {
                    Text(
                        text = stringResource(R.string.direct_customer_total_invalid),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.direct_customer_paid_total,
                            if (paidTotal == 0L) "0" else centsToInputText(paidTotal)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))
            }

            // 4. Platform Quoted Distance (Comparison input)
            OutlinedTextField(
                value = quotedDistanceKmText,
                onValueChange = { quotedDistanceKmText = it },
                label = { Text("App Quoted Distance (km)") },
                placeholder = { Text("e.g. 4.2") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(CockpitDimens.CardCornerRadius)
            )

            // Live Discrepancy Preview
            val disc = discrepancyMeters
            if (disc != null) {
                Spacer(modifier = Modifier.height(CockpitDimens.SpacingSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Odometer Reality:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DiscrepancyBadge(differenceMeters = disc)
                }
            }

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingLarge))

            // 5. Giant Complete Button (UI-001: 64dp height)
            CockpitButton(
                text = "Save & Finish Run",
                onClick = {
                    val platformFeeCents = parseInputAmountToCents(platformFeeText)
                    val cashCents = if (hasCash) parseInputAmountToCents(cashCollectedText) else 0L

                    onCompleteTrip(
                        endAddress.ifBlank { "Destination" },
                        platformFeeCents,
                        cashCents,
                        quotedDistanceMeters,
                        notesText
                    )
                },
                containerColor = GreenProfit,
                enabled = !isDirectTrip || customerPaidTotalCents != null
            )
        }
    }
}

internal fun centsToInputText(cents: Long): String {
    if (cents <= 0L) return ""
    return BigDecimal.valueOf(cents, 2).stripTrailingZeros().toPlainString()
}

internal fun metersToInputText(meters: Double?): String {
    if (meters == null || meters <= 0.0) return ""
    return BigDecimal.valueOf(meters / 1000.0).stripTrailingZeros().toPlainString()
}

internal fun parseInputAmountToCents(text: String): Long {
    return parseNonNegativeAmountToCents(text) ?: 0L
}

internal fun directCustomerPaidTotalCents(
    transferAmountText: String,
    cashAmountText: String,
    hasCash: Boolean
): Long? {
    if (transferAmountText.isBlank() && !hasCash) return null
    if (hasCash && cashAmountText.isBlank()) return null
    val transferCents = parseNonNegativeAmountToCents(transferAmountText) ?: return null
    val cashCents = if (hasCash) parseNonNegativeAmountToCents(cashAmountText) ?: return null else 0L
    return runCatching { Math.addExact(transferCents, cashCents) }.getOrNull()
}

private fun parseNonNegativeAmountToCents(text: String): Long? {
    if (text.isBlank()) return 0L
    return runCatching {
        val amount = text.toBigDecimal()
        if (amount.signum() < 0) return null
        amount.movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    }.getOrNull()
}
