package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.local.RecentDestinationEntity
import com.example.model.PoiCategory
import com.example.model.PoiItem
import com.example.ui.theme.CategoryEvColor
import com.example.ui.theme.CategoryGasColor
import com.example.ui.theme.CategoryParkingColor
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CockpitSurfaceVariant
import com.example.ui.theme.CockpitTextMuted
import com.example.ui.theme.CockpitTextPrimary
import com.example.ui.theme.CockpitTextSecondary
import com.example.ui.theme.NavRouteCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDestinationDialog(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    searchResults: List<PoiItem>,
    recentDestinations: List<RecentDestinationEntity>,
    onSelectDestination: (PoiItem) -> Unit,
    onSelectRecent: (RecentDestinationEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CockpitSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.25f))
            )
        },
        modifier = modifier.testTag("search_destination_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Input Field (driver-friendly large typography)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChanged,
                placeholder = {
                    Text(
                        "Where to? (e.g. Airport, Starbucks, Gas)",
                        color = CockpitTextMuted,
                        fontSize = 15.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NavRouteCyan,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = CockpitTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavRouteCyan,
                    unfocusedBorderColor = CockpitBorder,
                    focusedContainerColor = CockpitSurfaceVariant,
                    unfocusedContainerColor = CockpitSurfaceVariant,
                    focusedTextColor = CockpitTextPrimary,
                    unfocusedTextColor = CockpitTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick 1-Tap Preset Places (Home, Work, Airport, Gas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickPlaceChip(
                    icon = Icons.Default.Home,
                    label = "Home",
                    onClick = {
                        val home = recentDestinations.firstOrNull { it.category == "HOME" }
                        if (home != null) onSelectRecent(home) else onQueryChanged("Home")
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickPlaceChip(
                    icon = Icons.Default.Work,
                    label = "Work",
                    onClick = {
                        val work = recentDestinations.firstOrNull { it.category == "WORK" }
                        if (work != null) onSelectRecent(work) else onQueryChanged("Work Office")
                    },
                    modifier = Modifier.weight(1f)
                )
                QuickPlaceChip(
                    icon = Icons.Default.LocalGasStation,
                    label = "Gas",
                    onClick = { onQueryChanged("Gas") },
                    modifier = Modifier.weight(1f)
                )
                QuickPlaceChip(
                    icon = Icons.Default.ElectricCar,
                    label = "EV Hub",
                    onClick = { onQueryChanged("Tesla") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (searchQuery.isBlank() && recentDestinations.isNotEmpty()) {
                    item {
                        Text(
                            text = "RECENT DESTINATIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CockpitTextMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(recentDestinations) { item ->
                        RecentDestinationRow(
                            destination = item,
                            onClick = { onSelectRecent(item) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "POPULAR DESTINATIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CockpitTextMuted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                items(searchResults) { poi ->
                    PoiResultRow(
                        poi = poi,
                        onClick = { onSelectDestination(poi) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickPlaceChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CockpitSurfaceVariant)
            .border(1.dp, CockpitBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = NavRouteCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CockpitTextPrimary
        )
    }
}

@Composable
fun PoiResultRow(
    poi: PoiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon = when (poi.category) {
        PoiCategory.GAS -> Icons.Default.LocalGasStation
        PoiCategory.EV_CHARGING -> Icons.Default.ElectricCar
        PoiCategory.PARKING -> Icons.Default.LocalParking
        else -> Icons.Default.Place
    }
    val categoryColor = when (poi.category) {
        PoiCategory.GAS -> CategoryGasColor
        PoiCategory.EV_CHARGING -> CategoryEvColor
        PoiCategory.PARKING -> CategoryParkingColor
        else -> NavRouteCyan
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CockpitSurfaceVariant)
            .border(1.dp, CockpitBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("poi_item_${poi.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = poi.name,
                tint = categoryColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = poi.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CockpitTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = poi.address,
                fontSize = 12.sp,
                color = CockpitTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = poi.extraInfo,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = NavRouteCyan,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${poi.distanceKm} km",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = CockpitTextPrimary
            )
            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = "Navigate",
                tint = NavRouteCyan,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun RecentDestinationRow(
    destination: RecentDestinationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CockpitSurfaceVariant)
            .border(1.dp, CockpitBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("recent_item_${destination.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = "Recent",
            tint = CockpitTextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = destination.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = CockpitTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = destination.address,
                fontSize = 12.sp,
                color = CockpitTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
