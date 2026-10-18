package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SpeedUnit
import com.example.ui.components.CockpitFloatingControls
import com.example.ui.components.EmergencySosDialog
import com.example.ui.components.HudDisplayView
import com.example.ui.components.InCarMediaBar
import com.example.ui.components.MapCanvas
import com.example.ui.components.QuickPoiBar
import com.example.ui.components.SearchDestinationDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SpeedometerGauge
import com.example.ui.components.TripOverviewSheet
import com.example.ui.components.TurnGuidanceBanner
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitSurface
import com.example.ui.theme.CockpitSurfaceVariant
import com.example.ui.theme.CockpitTextMuted
import com.example.ui.theme.CockpitTextPrimary
import com.example.ui.theme.CockpitTextSecondary
import com.example.ui.theme.NavRouteCyan
import com.example.ui.theme.SpeedDanger

@Composable
fun AutoNavScreen(
    viewModel: AutoNavViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Observe state flows
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val currentSpeedKmh by viewModel.currentSpeedKmh.collectAsStateWithLifecycle()
    val speedLimitKmh by viewModel.speedLimitKmh.collectAsStateWithLifecycle()
    val currentHeading by viewModel.currentHeading.collectAsStateWithLifecycle()
    val speedUnit by viewModel.speedUnit.collectAsStateWithLifecycle()
    val isLockedToCar by viewModel.isLockedToCar.collectAsStateWithLifecycle()
    val isVoiceMuted by viewModel.isVoiceMuted.collectAsStateWithLifecycle()
    val isHudActive by viewModel.isHudActive.collectAsStateWithLifecycle()
    val isSimulating by viewModel.isSimulating.collectAsStateWithLifecycle()
    val keepScreenAwake by viewModel.keepScreenAwake.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val activeRoute by viewModel.activeRoute.collectAsStateWithLifecycle()
    val currentStepIndex by viewModel.currentStepIndex.collectAsStateWithLifecycle()
    val distanceToNextManeuverMeters by viewModel.distanceToNextManeuverMeters.collectAsStateWithLifecycle()
    val remainingDistanceMeters by viewModel.remainingDistanceMeters.collectAsStateWithLifecycle()
    val etaString by viewModel.etaString.collectAsStateWithLifecycle()
    val drivingAlerts by viewModel.drivingAlerts.collectAsStateWithLifecycle()

    val showSearchDialog by viewModel.showSearchDialog.collectAsStateWithLifecycle()
    val showTripOverview by viewModel.showTripOverview.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showSosDialog by viewModel.showSosDialog.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val recentDestinations by viewModel.recentDestinations.collectAsStateWithLifecycle()
    val nearbyPois by viewModel.nearbyPois.collectAsStateWithLifecycle()
    val selectedRouteType by viewModel.selectedRouteType.collectAsStateWithLifecycle()

    // Request location permissions gracefully
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.locationHelper.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Back button behavior
    BackHandler(enabled = isHudActive || showTripOverview || showSearchDialog || showSosDialog || showSettingsDialog) {
        when {
            isHudActive -> viewModel.toggleHud()
            showSosDialog -> viewModel.closeSos()
            showSettingsDialog -> viewModel.closeSettings()
            showTripOverview -> viewModel.closeTripOverview()
            showSearchDialog -> viewModel.closeSearch()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isLandscape = maxWidth > maxHeight

            // 1. Vector Map Canvas (Background)
            MapCanvas(
                carLocation = currentLocation,
                carHeading = currentHeading,
                activeRoute = activeRoute,
                nearbyPois = nearbyPois,
                alerts = drivingAlerts,
                isLockedToCar = isLockedToCar,
                is3dPerspective = true,
                onMapInteracted = { viewModel.onMapInteracted() },
                modifier = Modifier.fillMaxSize()
            )

            // 2. Cockpit Overlay Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header / Navigation Guidance
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeRoute != null) {
                        val currentStep = activeRoute?.steps?.getOrNull(currentStepIndex)
                        val nextStep = activeRoute?.steps?.getOrNull(currentStepIndex + 1)
                        TurnGuidanceBanner(
                            currentStep = currentStep,
                            nextStep = nextStep,
                            distanceToNextManeuverMeters = distanceToNextManeuverMeters,
                            speedUnit = speedUnit
                        )
                    } else {
                        // Inactive Navigation Search Bar Header
                        IdleDashboardHeader(
                            onOpenSearch = { viewModel.openSearch() },
                            onSelectHome = {
                                val home = recentDestinations.firstOrNull { it.category == "HOME" }
                                if (home != null) viewModel.selectRecentDestination(home) else viewModel.openSearch()
                            },
                            onSelectWork = {
                                val work = recentDestinations.firstOrNull { it.category == "WORK" }
                                if (work != null) viewModel.selectRecentDestination(work) else viewModel.openSearch()
                            }
                        )
                    }
                }

                // Middle Area: Speedometer & Floating Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left: Large Digital Speedometer Gauge
                    SpeedometerGauge(
                        currentSpeedKmh = currentSpeedKmh,
                        speedLimitKmh = speedLimitKmh,
                        speedUnit = speedUnit,
                        headingDegrees = currentHeading,
                        onToggleSpeedUnit = { viewModel.toggleSpeedUnit() }
                    )

                    // Right: Quick Floating Cockpit Controls
                    CockpitFloatingControls(
                        isVoiceMuted = isVoiceMuted,
                        isHudActive = isHudActive,
                        isLockedToCar = isLockedToCar,
                        isSimulating = isSimulating,
                        hasActiveRoute = activeRoute != null,
                        onToggleVoice = { viewModel.toggleVoice() },
                        onToggleHud = { viewModel.toggleHud() },
                        onRecenter = { viewModel.recenterCar() },
                        onToggleSimulation = {
                            if (isSimulating) viewModel.stopNavigation() else viewModel.startDriveSimulation()
                        },
                        onOpenSearch = { viewModel.openSearch() },
                        onOpenSettings = { viewModel.openSettings() },
                        onStopNavigation = { viewModel.stopNavigation() }
                    )
                }

                // Bottom Strip: Trip Progress Bar (if active) OR Quick POI & Media Bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (activeRoute != null) {
                        ActiveTripSummaryBar(
                            activeRoute = activeRoute!!,
                            remainingMeters = remainingDistanceMeters,
                            etaString = etaString,
                            speedUnit = speedUnit,
                            isSimulating = isSimulating,
                            onStop = { viewModel.stopNavigation() }
                        )
                    }

                    // Quick POI Categories Bar (Gas, EV, Parking, Food, SOS)
                    QuickPoiBar(
                        onSelectCategory = { cat -> viewModel.selectPoiCategory(cat) },
                        onOpenEmergencySos = { viewModel.openSos() }
                    )

                    // In-Car Media Player Bar
                    if (!isLandscape) {
                        InCarMediaBar()
                    }
                }
            }

            // 3. Fullscreen Windshield HUD Mode Overlay
            AnimatedVisibility(
                visible = isHudActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                HudDisplayView(
                    currentSpeedKmh = currentSpeedKmh,
                    speedLimitKmh = speedLimitKmh,
                    speedUnit = speedUnit,
                    currentStep = activeRoute?.steps?.getOrNull(currentStepIndex),
                    distanceToNextManeuverMeters = distanceToNextManeuverMeters,
                    etaString = etaString,
                    onCloseHud = { viewModel.toggleHud() }
                )
            }

            // 4. Search Modal Bottom Sheet
            if (showSearchDialog) {
                SearchDestinationDialog(
                    searchQuery = searchQuery,
                    onQueryChanged = { viewModel.search(it) },
                    searchResults = searchResults,
                    recentDestinations = recentDestinations,
                    onSelectDestination = { viewModel.selectDestination(it) },
                    onSelectRecent = { viewModel.selectRecentDestination(it) },
                    onDismiss = { viewModel.closeSearch() }
                )
            }

            // 5. Trip Overview Route Alternatives Sheet
            if (showTripOverview && activeRoute != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { viewModel.closeTripOverview() },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    TripOverviewSheet(
                        activeRoute = activeRoute!!,
                        speedUnit = speedUnit,
                        selectedRouteType = selectedRouteType,
                        onSelectRouteType = { viewModel.selectRouteType(it) },
                        onStartNavigation = { viewModel.startNavigation() },
                        onStartSimulation = { viewModel.startDriveSimulation() },
                        onDismiss = { viewModel.closeTripOverview() }
                    )
                }
            }

            // 6. Emergency SOS Roadside Dialog
            if (showSosDialog) {
                EmergencySosDialog(
                    currentLocation = currentLocation,
                    onDismiss = { viewModel.closeSos() }
                )
            }

            // 7. Settings Dialog
            if (showSettingsDialog) {
                SettingsDialog(
                    speedUnit = speedUnit,
                    isVoiceEnabled = !isVoiceMuted,
                    keepScreenAwake = keepScreenAwake,
                    isDarkTheme = isDarkTheme,
                    onSpeedUnitChanged = { viewModel.toggleSpeedUnit() },
                    onVoiceEnabledChanged = { viewModel.toggleVoice() },
                    onKeepScreenAwakeChanged = { viewModel.setKeepScreenAwake(it) },
                    onDarkThemeChanged = { viewModel.setDarkTheme(it) },
                    onDismiss = { viewModel.closeSettings() }
                )
            }
        }
    }
}

@Composable
fun IdleDashboardHeader(
    onOpenSearch: () -> Unit,
    onSelectHome: () -> Unit,
    onSelectWork: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CockpitSurface.copy(alpha = 0.95f))
            .border(1.5.dp, CockpitBorder, RoundedCornerShape(20.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Clickable Input
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(CockpitSurfaceVariant)
                .clickable { onOpenSearch() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("dashboard_search_bar"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = NavRouteCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Where to? (Search destination)",
                fontSize = 14.sp,
                color = CockpitTextMuted
            )
        }

        // Quick 1-Tap Home Button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CockpitSurfaceVariant)
                .clickable { onSelectHome() }
                .testTag("header_quick_home"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = NavRouteCyan,
                modifier = Modifier.size(22.dp)
            )
        }

        // Quick 1-Tap Work Button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CockpitSurfaceVariant)
                .clickable { onSelectWork() }
                .testTag("header_quick_work"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Work,
                contentDescription = "Work",
                tint = NavRouteCyan,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun ActiveTripSummaryBar(
    activeRoute: com.example.model.NavRoute,
    remainingMeters: Int,
    etaString: String,
    speedUnit: SpeedUnit,
    isSimulating: Boolean,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingFormatted = if (speedUnit == SpeedUnit.KMH) {
        String.format(java.util.Locale.US, "%.1f km", remainingMeters / 1000.0)
    } else {
        String.format(java.util.Locale.US, "%.1f mi", (remainingMeters * 0.000621371))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CockpitSurface.copy(alpha = 0.95f))
            .border(1.dp, CockpitBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("active_trip_summary_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NavRouteCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Active Trip",
                    tint = NavRouteCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ETA $etaString",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = CockpitTextPrimary
                    )
                    if (isSimulating) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• SIMULATING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavRouteCyan
                        )
                    }
                }
                Text(
                    text = "$remainingFormatted remaining • ${activeRoute.destinationName}",
                    fontSize = 12.sp,
                    color = CockpitTextSecondary,
                    maxLines = 1
                )
            }
        }

        // Exit / Stop Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SpeedDanger.copy(alpha = 0.2f))
                .clickable { onStop() }
                .testTag("summary_stop_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit Navigation",
                tint = SpeedDanger,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
