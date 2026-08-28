package com.ontheroad.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ontheroad.core.ui.theme.AmberWarning
import com.ontheroad.core.ui.theme.AmberWarningContainer
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.GreenProfit
import com.ontheroad.core.ui.theme.GreenProfitContainer
import com.ontheroad.core.ui.theme.RedDiscrepancy
import com.ontheroad.core.ui.theme.RedDiscrepancyContainer

@Composable
fun DiscrepancyBadge(
    differenceMeters: Double,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, label) = when {
        differenceMeters > 500.0 -> {
            val km = String.format("%.1f", differenceMeters / 1000.0)
            Triple(RedDiscrepancyContainer, RedDiscrepancy, "+$km km uncompensated")
        }
        differenceMeters >= 50.0 -> {
            val km = String.format("%.1f", differenceMeters / 1000.0)
            Triple(AmberWarningContainer, AmberWarning, "+$km km detour")
        }
        differenceMeters <= -50.0 -> {
            val km = String.format("%.1f", kotlin.math.abs(differenceMeters) / 1000.0)
            Triple(GreenProfitContainer, GreenProfit, "-$km km saved")
        }
        else -> {
            Triple(GreenProfitContainer, GreenProfit, "Matches quote")
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(CockpitDimens.PillCornerRadius))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
