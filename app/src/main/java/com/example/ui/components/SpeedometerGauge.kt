package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeedUnit
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CockpitTextMuted
import com.example.ui.theme.CockpitTextPrimary
import com.example.ui.theme.CockpitTextSecondary
import com.example.ui.theme.SpeedDanger
import com.example.ui.theme.SpeedSafe
import com.example.ui.theme.SpeedWarning
import kotlin.math.roundToInt

@Composable
fun SpeedometerGauge(
    currentSpeedKmh: Float,
    speedLimitKmh: Int,
    speedUnit: SpeedUnit,
    headingDegrees: Float,
    onToggleSpeedUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displaySpeed = (currentSpeedKmh * speedUnit.multiplier).roundToInt().coerceAtLeast(0)
    val displayLimit = (speedLimitKmh * speedUnit.multiplier).roundToInt()
    val isOverSpeed = displayLimit > 0 && displaySpeed > displayLimit
    val isNearSpeed = displayLimit > 0 && displaySpeed in (displayLimit - 5)..displayLimit

    val speedColor by animateColorAsState(
        targetValue = when {
            isOverSpeed -> SpeedDanger
            isNearSpeed -> SpeedWarning
            else -> SpeedSafe
        },
        label = "speedColor"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CockpitSurface.copy(alpha = 0.92f))
            .border(1.5.dp, if (isOverSpeed) SpeedDanger else Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("speedometer_gauge"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Digital Speedometer Display
        Column(
            modifier = Modifier.clickable { onToggleSpeedUnit() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$displaySpeed",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    color = speedColor,
                    lineHeight = 44.sp,
                    modifier = Modifier.testTag("speed_value_text")
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = speedUnit.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CockpitTextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            if (isOverSpeed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Overspeed",
                        tint = SpeedDanger,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "SLOW DOWN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SpeedDanger
                    )
                }
            } else {
                Text(
                    text = "TAP FOR UNIT",
                    fontSize = 9.sp,
                    color = CockpitTextMuted
                )
            }
        }

        // Circular Speed Limit Sign Badge
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(4.dp, Color(0xFFDC2626), CircleShape)
                .testTag("speed_limit_sign"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$displayLimit",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
        }

        // Compass Heading
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Heading",
                tint = CockpitTextPrimary,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(headingDegrees)
            )
            Text(
                text = getCardinalDirection(headingDegrees),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CockpitTextPrimary
            )
            Text(
                text = "${headingDegrees.toInt()}°",
                fontSize = 9.sp,
                color = CockpitTextMuted
            )
        }
    }
}

private fun getCardinalDirection(degrees: Float): String {
    val norm = ((degrees % 360) + 360) % 360
    return when {
        norm >= 337.5 || norm < 22.5 -> "N"
        norm in 22.5..67.5 -> "NE"
        norm in 67.5..112.5 -> "E"
        norm in 112.5..157.5 -> "SE"
        norm in 157.5..202.5 -> "S"
        norm in 202.5..247.5 -> "SW"
        norm in 247.5..292.5 -> "W"
        else -> "NW"
    }
}
