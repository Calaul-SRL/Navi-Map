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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CockpitTextPrimary
import com.example.ui.theme.CockpitTextSecondary
import com.example.ui.theme.NavRouteCyan
import com.example.ui.theme.SpeedDanger
import com.example.ui.theme.SpeedSafe

@Composable
fun CockpitFloatingControls(
    isVoiceMuted: Boolean,
    isHudActive: Boolean,
    isLockedToCar: Boolean,
    isSimulating: Boolean,
    hasActiveRoute: Boolean,
    onToggleVoice: () -> Unit,
    onToggleHud: () -> Unit,
    onRecenter: () -> Unit,
    onToggleSimulation: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onStopNavigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Search "Where To?" Button (Large 54dp car touch target)
        CockpitRoundButton(
            icon = Icons.Default.Search,
            contentDescription = "Search Destination",
            backgroundColor = CockpitSurface.copy(alpha = 0.95f),
            iconTint = NavRouteCyan,
            badge = "FIND",
            onClick = onOpenSearch,
            testTag = "search_destination_button"
        )

        // Windshield HUD Button
        CockpitRoundButton(
            icon = Icons.Default.Lightbulb,
            contentDescription = "Windshield HUD Mode",
            backgroundColor = if (isHudActive) NavRouteCyan else CockpitSurface.copy(alpha = 0.95f),
            iconTint = if (isHudActive) Color.Black else CockpitTextPrimary,
            badge = "HUD",
            onClick = onToggleHud,
            testTag = "hud_toggle_button"
        )

        // Voice Guidance Mute Button
        CockpitRoundButton(
            icon = if (isVoiceMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Voice Guidance",
            backgroundColor = CockpitSurface.copy(alpha = 0.95f),
            iconTint = if (isVoiceMuted) CockpitTextSecondary else SpeedSafe,
            badge = if (isVoiceMuted) "MUTED" else "VOICE",
            onClick = onToggleVoice,
            testTag = "voice_guidance_button"
        )

        // Simulation / Drive Test Run Button
        if (hasActiveRoute) {
            CockpitRoundButton(
                icon = if (isSimulating) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = "Drive Simulation",
                backgroundColor = if (isSimulating) SpeedSafe.copy(alpha = 0.9f) else CockpitSurface.copy(alpha = 0.95f),
                iconTint = if (isSimulating) Color.Black else NavRouteCyan,
                badge = if (isSimulating) "DRIVING" else "PREVIEW",
                onClick = onToggleSimulation,
                testTag = "drive_simulation_button"
            )
        }

        // Recenter Vehicle Location Button
        CockpitRoundButton(
            icon = Icons.Default.MyLocation,
            contentDescription = "Recenter Car",
            backgroundColor = if (isLockedToCar) NavRouteCyan.copy(alpha = 0.2f) else CockpitSurface.copy(alpha = 0.95f),
            iconTint = if (isLockedToCar) NavRouteCyan else CockpitTextSecondary,
            badge = "CENTER",
            onClick = onRecenter,
            testTag = "recenter_car_button"
        )

        // Settings Button
        CockpitRoundButton(
            icon = Icons.Default.Settings,
            contentDescription = "Cockpit Settings",
            backgroundColor = CockpitSurface.copy(alpha = 0.95f),
            iconTint = CockpitTextPrimary,
            badge = null,
            onClick = onOpenSettings,
            testTag = "cockpit_settings_button"
        )

        // Stop Navigation (Exit Route)
        if (hasActiveRoute) {
            CockpitRoundButton(
                icon = Icons.Default.Close,
                contentDescription = "Stop Navigation",
                backgroundColor = SpeedDanger.copy(alpha = 0.9f),
                iconTint = Color.White,
                badge = "EXIT",
                onClick = onStopNavigation,
                testTag = "stop_navigation_button"
            )
        }
    }
}

@Composable
fun CockpitRoundButton(
    icon: ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    iconTint: Color,
    badge: String?,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(1.5.dp, CockpitBorder, CircleShape)
                .clickable { onClick() }
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        if (badge != null) {
            Text(
                text = badge,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = CockpitTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
