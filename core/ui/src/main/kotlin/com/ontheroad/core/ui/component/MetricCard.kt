package com.ontheroad.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ontheroad.core.ui.theme.CockpitDimens
import com.ontheroad.core.ui.theme.OnSurfaceSecondary
import com.ontheroad.core.ui.theme.OnSurfaceWhite

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    subtitle: String? = null,
    badge: (@Composable () -> Unit)? = null,
    valueColor: Color = OnSurfaceWhite
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(CockpitDimens.MetricCardMinHeight),
        shape = RoundedCornerShape(CockpitDimens.CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CockpitDimens.SpacingMedium),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = OnSurfaceSecondary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                if (badge != null) {
                    badge()
                }
            }

            Spacer(modifier = Modifier.height(CockpitDimens.SpacingXSmall))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    color = valueColor,
                    fontWeight = FontWeight.ExtraBold
                )
                if (unit != null) {
                    Text(
                        text = " $unit",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnSurfaceSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceSecondary
                )
            }
        }
    }
}
