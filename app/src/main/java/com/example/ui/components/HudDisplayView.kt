package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RouteStep
import com.example.model.SpeedUnit
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudNeonGreen
import com.example.ui.theme.HudNeonRed
import kotlin.math.roundToInt

@Composable
fun HudDisplayView(
    currentSpeedKmh: Float,
    speedLimitKmh: Int,
    speedUnit: SpeedUnit,
    currentStep: RouteStep?,
    distanceToNextManeuverMeters: Int,
    etaString: String,
    onCloseHud: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMirrored by remember { mutableStateOf(false) }

    val displaySpeed = (currentSpeedKmh * speedUnit.multiplier).roundToInt().coerceAtLeast(0)
    val displayLimit = (speedLimitKmh * speedUnit.multiplier).roundToInt()
    val isOverSpeed = displayLimit > 0 && displaySpeed > displayLimit

    val neonColor = if (isOverSpeed) HudNeonRed else HudNeonGreen

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .testTag("hud_display_view")
    ) {
        // Mirrored or Direct content container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Mirror horizontally when laid on dashboard to reflect correctly onto glass
                    if (isMirrored) {
                        scaleX = -1f
                    }
                }
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Turn Arrow & Street Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = getManeuverIcon(currentStep.maneuver),
                            contentDescription = "Maneuver",
                            tint = neonColor,
                            modifier = Modifier.size(54.dp)
                        )
                        Column {
                            Text(
                                text = formatManeuverDistance(distanceToNextManeuverMeters, speedUnit),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = neonColor
                            )
                            Text(
                                text = currentStep.streetName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = neonColor.copy(alpha = 0.85f)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "AUTONAV HUD COCKPIT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = neonColor.copy(alpha = 0.7f)
                    )
                }

                // Speed Limit Round Sign (HUD Outline)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .graphicsLayer {
                            // Border in compose
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LIMIT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = neonColor
                        )
                        Text(
                            text = "$displayLimit",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = neonColor
                        )
                    }
                }
            }

            // Center: Gigantic HUD Digital Speedometer (ultra glanceable on windshield)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    text = "$displaySpeed",
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = neonColor,
                    lineHeight = 120.sp,
                    modifier = Modifier.testTag("hud_speed_text")
                )
                Text(
                    text = speedUnit.label.uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = neonColor.copy(alpha = 0.8f)
                )

                if (isOverSpeed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = HudNeonRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SPEED ALERT",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = HudNeonRed
                        )
                    }
                }
            }

            // Bottom Bar: ETA & Destination info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ESTIMATED ARRIVAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = neonColor.copy(alpha = 0.6f)
                    )
                    Text(
                        text = etaString,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = neonColor
                    )
                }

                Text(
                    text = if (isMirrored) "WINDSHIELD PROJECTION ON" else "DIRECT DASH VIEW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = neonColor.copy(alpha = 0.7f)
                )
            }
        }

        // Overlay Action Buttons (Direct touch controls, NOT mirrored)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { isMirrored = !isMirrored },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.18f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("hud_mirror_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Flip,
                    contentDescription = "Mirror Windshield",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isMirrored) "Normal View" else "Mirror for Windshield",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onCloseHud,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.18f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("hud_exit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit HUD",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Exit HUD",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
