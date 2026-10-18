package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavRoute
import com.example.model.RouteType
import com.example.model.SpeedUnit
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CockpitSurfaceVariant
import com.example.ui.theme.CockpitTextMuted
import com.example.ui.theme.CockpitTextPrimary
import com.example.ui.theme.CockpitTextSecondary
import com.example.ui.theme.NavRouteCyan
import com.example.ui.theme.SpeedSafe

@Composable
fun TripOverviewSheet(
    activeRoute: NavRoute,
    speedUnit: SpeedUnit,
    selectedRouteType: RouteType,
    onSelectRouteType: (RouteType) -> Unit,
    onStartNavigation: () -> Unit,
    onStartSimulation: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(CockpitSurface)
            .border(1.5.dp, CockpitBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(18.dp)
            .testTag("trip_overview_sheet")
    ) {
        // Drag Handle / Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activeRoute.destinationName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CockpitTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = activeRoute.destinationAddress,
                    fontSize = 13.sp,
                    color = CockpitTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = CockpitTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Route Type Tabs (Fastest, Shortest, Eco)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RouteTypeCard(
                title = "Fastest",
                durationMin = (activeRoute.totalDurationSeconds / 60),
                routeType = RouteType.FASTEST,
                isSelected = selectedRouteType == RouteType.FASTEST,
                onSelect = { onSelectRouteType(RouteType.FASTEST) },
                modifier = Modifier.weight(1f)
            )
            RouteTypeCard(
                title = "Shortest",
                durationMin = (activeRoute.totalDurationSeconds / 60) + 4,
                routeType = RouteType.SHORTEST,
                isSelected = selectedRouteType == RouteType.SHORTEST,
                onSelect = { onSelectRouteType(RouteType.SHORTEST) },
                modifier = Modifier.weight(1f)
            )
            RouteTypeCard(
                title = "Eco Path",
                durationMin = (activeRoute.totalDurationSeconds / 60) + 2,
                routeType = RouteType.ECO_FRIENDLY,
                isSelected = selectedRouteType == RouteType.ECO_FRIENDLY,
                onSelect = { onSelectRouteType(RouteType.ECO_FRIENDLY) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Distance & Traffic Summary Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CockpitSurfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val distFormatted = if (speedUnit == SpeedUnit.KMH) {
                String.format(java.util.Locale.US, "%.1f km", activeRoute.totalDistanceMeters / 1000.0)
            } else {
                String.format(java.util.Locale.US, "%.1f mi", (activeRoute.totalDistanceMeters * 0.000621371))
            }
            Text(
                text = "Total: $distFormatted",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CockpitTextPrimary
            )
            Text(
                text = if (activeRoute.tollFree) "No Tolls" else "Tolls on Route",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SpeedSafe
            )
            Text(
                text = "Live Traffic: Smooth",
                fontSize = 12.sp,
                color = NavRouteCyan
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Driver Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onStartNavigation,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("start_drive_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavRouteCyan,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Start",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START DRIVE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            OutlinedButton(
                onClick = onStartSimulation,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("preview_simulation_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NavRouteCyan
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Preview",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PREVIEW",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RouteTypeCard(
    title: String,
    durationMin: Int,
    routeType: RouteType,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) NavRouteCyan.copy(alpha = 0.15f) else CockpitSurfaceVariant)
            .border(
                1.5.dp,
                if (isSelected) NavRouteCyan else CockpitBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onSelect() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) NavRouteCyan else CockpitTextSecondary
        )
        Text(
            text = "$durationMin min",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = CockpitTextPrimary
        )
    }
}
