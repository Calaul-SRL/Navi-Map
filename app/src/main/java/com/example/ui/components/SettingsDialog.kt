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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.SpeedUnit
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CockpitSurfaceVariant
import com.example.ui.theme.CockpitTextMuted
import com.example.ui.theme.CockpitTextPrimary
import com.example.ui.theme.CockpitTextSecondary
import com.example.ui.theme.NavRouteCyan

@Composable
fun SettingsDialog(
    speedUnit: SpeedUnit,
    isVoiceEnabled: Boolean,
    keepScreenAwake: Boolean,
    isDarkTheme: Boolean,
    onSpeedUnitChanged: (SpeedUnit) -> Unit,
    onVoiceEnabledChanged: (Boolean) -> Unit,
    onKeepScreenAwakeChanged: (Boolean) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CockpitSurface)
                .border(1.5.dp, CockpitBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("cockpit_settings_dialog")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Car Cockpit Settings",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = CockpitTextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CockpitTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Speed Units (KM/H vs MPH)
            Text(
                text = "SPEED & DISTANCE UNIT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CockpitTextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UnitOptionCard(
                    title = "Kilometers (km/h)",
                    isSelected = speedUnit == SpeedUnit.KMH,
                    onClick = { onSpeedUnitChanged(SpeedUnit.KMH) },
                    modifier = Modifier.weight(1f)
                )
                UnitOptionCard(
                    title = "Miles (mph)",
                    isSelected = speedUnit == SpeedUnit.MPH,
                    onClick = { onSpeedUnitChanged(SpeedUnit.MPH) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Toggle Switches
            SettingToggleRow(
                title = "Voice Turn Navigation",
                subtitle = "Spoken maneuvers and speed limit warnings",
                checked = isVoiceEnabled,
                onCheckedChange = onVoiceEnabledChanged
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingToggleRow(
                title = "Keep Screen Awake (Car Mount)",
                subtitle = "Prevents display timeout while driving",
                checked = keepScreenAwake,
                onCheckedChange = onKeepScreenAwakeChanged
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingToggleRow(
                title = "Cockpit Night Mode",
                subtitle = "Optimized dark OLED theme for windshield night driving",
                checked = isDarkTheme,
                onCheckedChange = onDarkThemeChanged
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavRouteCyan, contentColor = Color.Black)
            ) {
                Text(
                    text = "DONE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CockpitTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = CockpitTextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = NavRouteCyan,
                uncheckedThumbColor = CockpitTextSecondary,
                uncheckedTrackColor = CockpitSurfaceVariant
            )
        )
    }
}

@Composable
fun UnitOptionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) NavRouteCyan.copy(alpha = 0.15f) else CockpitSurfaceVariant)
            .border(
                1.5.dp,
                if (isSelected) NavRouteCyan else CockpitBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) NavRouteCyan else CockpitTextPrimary
        )
    }
}
