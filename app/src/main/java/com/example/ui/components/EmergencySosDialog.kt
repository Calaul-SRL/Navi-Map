package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GeoPoint
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CockpitSurfaceVariant
import com.example.ui.theme.CockpitTextMuted
import com.example.ui.theme.CockpitTextPrimary
import com.example.ui.theme.CockpitTextSecondary
import com.example.ui.theme.SpeedDanger
import com.example.ui.theme.SpeedWarning

@Composable
fun EmergencySosDialog(
    currentLocation: GeoPoint,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isHazardBeaconActive by remember { mutableStateOf(false) }
    var copiedCoordinates by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val beaconColor by infiniteTransition.animateColor(
        initialValue = SpeedWarning,
        targetValue = SpeedDanger,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconColor"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isHazardBeaconActive) beaconColor else Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = isHazardBeaconActive) {
                isHazardBeaconActive = false
            }
            .testTag("emergency_sos_dialog"),
        contentAlignment = Alignment.Center
    ) {
        if (isHazardBeaconActive) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Hazard Flasher",
                    tint = Color.Black,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "EMERGENCY HAZARD BEACON ACTIVE",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Place phone on dashboard facing windshield or road.\nTap anywhere to stop flashing.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CockpitSurface)
                    .border(2.dp, SpeedDanger, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SpeedDanger.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Emergency",
                                tint = SpeedDanger,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Emergency Roadside SOS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = CockpitTextPrimary
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

                Spacer(modifier = Modifier.height(16.dp))

                // GPS Location for Dispatcher
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CockpitSurfaceVariant)
                        .border(1.dp, CockpitBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "GPS",
                                tint = SpeedDanger,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CURRENT GPS COORDINATES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CockpitTextMuted
                            )
                        }
                        Text(
                            text = if (copiedCoordinates) "COPIED!" else "TAP TO COPY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (copiedCoordinates) SpeedWarning else CockpitTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val coordsText = String.format(
                        java.util.Locale.US,
                        "%.5f° N, %.5f° W",
                        currentLocation.lat,
                        Math.abs(currentLocation.lon)
                    )
                    Text(
                        text = coordsText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CockpitTextPrimary,
                        modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(coordsText))
                            copiedCoordinates = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hazard Flasher Beacon Button
                Button(
                    onClick = { isHazardBeaconActive = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("hazard_flasher_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpeedWarning,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Flasher",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START DASHBOARD HAZARD BEACON",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Call Emergency Roadside
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("call_911_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpeedDanger,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "911",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CALL 911",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Button(
                        onClick = {
                            // AAA or general roadside assistance dialer
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:18002224357"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("call_roadside_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CockpitSurfaceVariant,
                            contentColor = CockpitTextPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Roadside",
                            tint = SpeedWarning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ROADSIDE (AAA)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
