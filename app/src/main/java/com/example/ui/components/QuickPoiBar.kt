package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.Wc
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PoiCategory
import com.example.ui.theme.CategoryEvColor
import com.example.ui.theme.CategoryFoodColor
import com.example.ui.theme.CategoryGasColor
import com.example.ui.theme.CategoryParkingColor
import com.example.ui.theme.CategoryRestColor
import com.example.ui.theme.CategorySosColor
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CockpitTextPrimary

@Composable
fun QuickPoiBar(
    onSelectCategory: (PoiCategory) -> Unit,
    onOpenEmergencySos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickPoiPill(
            icon = Icons.Default.LocalGasStation,
            title = "Gas",
            color = CategoryGasColor,
            testTag = "quick_gas_button",
            onClick = { onSelectCategory(PoiCategory.GAS) }
        )

        QuickPoiPill(
            icon = Icons.Default.ElectricCar,
            title = "EV Charge",
            color = CategoryEvColor,
            testTag = "quick_ev_button",
            onClick = { onSelectCategory(PoiCategory.EV_CHARGING) }
        )

        QuickPoiPill(
            icon = Icons.Default.LocalParking,
            title = "Parking",
            color = CategoryParkingColor,
            testTag = "quick_parking_button",
            onClick = { onSelectCategory(PoiCategory.PARKING) }
        )

        QuickPoiPill(
            icon = Icons.Default.Restaurant,
            title = "Food",
            color = CategoryFoodColor,
            testTag = "quick_food_button",
            onClick = { onSelectCategory(PoiCategory.FOOD) }
        )

        QuickPoiPill(
            icon = Icons.Default.Wc,
            title = "Rest Stop",
            color = CategoryRestColor,
            testTag = "quick_rest_button",
            onClick = { onSelectCategory(PoiCategory.REST_STOP) }
        )

        QuickPoiPill(
            icon = Icons.Default.Sos,
            title = "SOS Care",
            color = CategorySosColor,
            testTag = "quick_sos_button",
            onClick = onOpenEmergencySos
        )
    }
}

@Composable
fun QuickPoiPill(
    icon: ImageVector,
    title: String,
    color: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CockpitSurface.copy(alpha = 0.95f))
            .border(1.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = CockpitTextPrimary
        )
    }
}
