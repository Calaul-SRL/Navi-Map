package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RoundaboutRight
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.TurnSlightLeft
import androidx.compose.material.icons.filled.TurnSlightRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LaneInfo
import com.example.model.ManeuverType
import com.example.model.RouteStep
import com.example.model.SpeedUnit
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CockpitTextMuted
import com.example.ui.theme.CockpitTextPrimary
import com.example.ui.theme.CockpitTextSecondary
import com.example.ui.theme.NavRouteCyan
import com.example.ui.theme.SpeedSafe

@Composable
fun TurnGuidanceBanner(
    currentStep: RouteStep?,
    nextStep: RouteStep?,
    distanceToNextManeuverMeters: Int,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    if (currentStep == null) return

    val distanceText = formatManeuverDistance(distanceToNextManeuverMeters, speedUnit)
    val maneuverIcon = getManeuverIcon(currentStep.maneuver)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CockpitSurface.copy(alpha = 0.95f))
            .border(1.5.dp, CockpitBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("turn_guidance_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Maneuver Turn Arrow Icon Box
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavRouteCyan.copy(alpha = 0.16f))
                    .border(2.dp, NavRouteCyan, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = maneuverIcon,
                    contentDescription = currentStep.instruction,
                    tint = NavRouteCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Distance Countdown & Primary Instruction
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = distanceText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = CockpitTextPrimary,
                    lineHeight = 30.sp,
                    modifier = Modifier.testTag("maneuver_distance_text")
                )
                Text(
                    text = currentStep.streetName.ifBlank { currentStep.instruction },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = CockpitTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentStep.instruction,
                    fontSize = 13.sp,
                    color = CockpitTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Secondary Next Maneuver Preview (if present)
            if (nextStep != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "THEN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CockpitTextMuted
                    )
                    Icon(
                        imageVector = getManeuverIcon(nextStep.maneuver),
                        contentDescription = "Next Turn",
                        tint = CockpitTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Lane Guidance Indicators (e.g. 4 highway lanes with arrows)
        if (currentStep.lanes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            LaneGuidanceBar(lanes = currentStep.lanes)
        }
    }
}

@Composable
fun LaneGuidanceBar(lanes: List<LaneInfo>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        lanes.forEach { lane ->
            val isRecommended = lane.isRecommended
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = 28.dp, height = 24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isRecommended) SpeedSafe.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                    .border(
                        1.dp,
                        if (isRecommended) SpeedSafe else Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (lane.directionArrow) {
                        "left" -> Icons.Default.TurnLeft
                        "slight_left" -> Icons.Default.TurnSlightLeft
                        "right" -> Icons.Default.TurnRight
                        "slight_right" -> Icons.Default.TurnSlightRight
                        else -> Icons.Default.ArrowUpward
                    },
                    contentDescription = null,
                    tint = if (isRecommended) SpeedSafe else CockpitTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

fun getManeuverIcon(maneuver: ManeuverType): ImageVector {
    return when (maneuver) {
        ManeuverType.DEPART -> Icons.Default.NearMe
        ManeuverType.STRAIGHT -> Icons.Default.ArrowUpward
        ManeuverType.TURN_SLIGHT_LEFT -> Icons.Default.TurnSlightLeft
        ManeuverType.TURN_LEFT -> Icons.Default.TurnLeft
        ManeuverType.TURN_SHARP_LEFT -> Icons.AutoMirrored.Filled.ArrowBack
        ManeuverType.TURN_SLIGHT_RIGHT -> Icons.Default.TurnSlightRight
        ManeuverType.TURN_RIGHT -> Icons.Default.TurnRight
        ManeuverType.TURN_SHARP_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
        ManeuverType.U_TURN -> Icons.Default.Refresh
        ManeuverType.ROUNDABOUT_ENTER, ManeuverType.ROUNDABOUT_EXIT -> Icons.Default.RoundaboutRight
        ManeuverType.MERGE -> Icons.AutoMirrored.Filled.CallMerge
        ManeuverType.HIGHWAY_ENTER -> Icons.AutoMirrored.Filled.CallMerge
        ManeuverType.HIGHWAY_EXIT -> Icons.AutoMirrored.Filled.CallSplit
        ManeuverType.ARRIVE -> Icons.Default.Place
    }
}

fun formatManeuverDistance(meters: Int, speedUnit: SpeedUnit): String {
    return if (speedUnit == SpeedUnit.KMH) {
        if (meters < 1000) {
            "${meters} m"
        } else {
            val km = meters / 1000.0
            String.format(java.util.Locale.US, "%.1f km", km)
        }
    } else {
        val miles = meters * 0.000621371
        if (miles < 0.1) {
            val feet = (meters * 3.28084).toInt()
            "${feet} ft"
        } else {
            String.format(java.util.Locale.US, "%.1f mi", miles)
        }
    }
}
