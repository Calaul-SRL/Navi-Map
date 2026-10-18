package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.RecentDestinationEntity
import com.example.data.repository.NavigationRepository
import com.example.model.DrivingAlert
import com.example.model.GeoPoint
import com.example.model.NavRoute
import com.example.model.PoiCategory
import com.example.model.PoiItem
import com.example.model.RouteStep
import com.example.model.RouteType
import com.example.model.SpeedUnit
import com.example.service.LocationHelper
import com.example.service.VoiceGuidanceService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AutoNavViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NavigationRepository()
    private val db = AppDatabase.getInstance(application)
    private val destinationDao = db.recentDestinationDao()

    val locationHelper = LocationHelper(application)
    val voiceService = VoiceGuidanceService(application)

    // Cockpit State
    private val _currentLocation = MutableStateFlow(repository.defaultStartLocation)
    val currentLocation: StateFlow<GeoPoint> = _currentLocation.asStateFlow()

    private val _currentSpeedKmh = MutableStateFlow(0f)
    val currentSpeedKmh: StateFlow<Float> = _currentSpeedKmh.asStateFlow()

    private val _speedLimitKmh = MutableStateFlow(65)
    val speedLimitKmh: StateFlow<Int> = _speedLimitKmh.asStateFlow()

    private val _currentHeading = MutableStateFlow(38f)
    val currentHeading: StateFlow<Float> = _currentHeading.asStateFlow()

    private val _speedUnit = MutableStateFlow(SpeedUnit.KMH)
    val speedUnit: StateFlow<SpeedUnit> = _speedUnit.asStateFlow()

    private val _isLockedToCar = MutableStateFlow(true)
    val isLockedToCar: StateFlow<Boolean> = _isLockedToCar.asStateFlow()

    private val _isVoiceMuted = MutableStateFlow(false)
    val isVoiceMuted: StateFlow<Boolean> = _isVoiceMuted.asStateFlow()

    private val _isHudActive = MutableStateFlow(false)
    val isHudActive: StateFlow<Boolean> = _isHudActive.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _keepScreenAwake = MutableStateFlow(true)
    val keepScreenAwake: StateFlow<Boolean> = _keepScreenAwake.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Active Navigation
    private val _activeRoute = MutableStateFlow<NavRoute?>(null)
    val activeRoute: StateFlow<NavRoute?> = _activeRoute.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    private val _distanceToNextManeuverMeters = MutableStateFlow(350)
    val distanceToNextManeuverMeters: StateFlow<Int> = _distanceToNextManeuverMeters.asStateFlow()

    private val _remainingDistanceMeters = MutableStateFlow(0)
    val remainingDistanceMeters: StateFlow<Int> = _remainingDistanceMeters.asStateFlow()

    private val _etaString = MutableStateFlow("--:--")
    val etaString: StateFlow<String> = _etaString.asStateFlow()

    private val _drivingAlerts = MutableStateFlow<List<DrivingAlert>>(emptyList())
    val drivingAlerts: StateFlow<List<DrivingAlert>> = _drivingAlerts.asStateFlow()

    // Dialog & UI Visibility
    private val _showSearchDialog = MutableStateFlow(false)
    val showSearchDialog: StateFlow<Boolean> = _showSearchDialog.asStateFlow()

    private val _showTripOverview = MutableStateFlow(false)
    val showTripOverview: StateFlow<Boolean> = _showTripOverview.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showSosDialog = MutableStateFlow(false)
    val showSosDialog: StateFlow<Boolean> = _showSosDialog.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<PoiItem>>(emptyList())
    val searchResults: StateFlow<List<PoiItem>> = _searchResults.asStateFlow()

    private val _recentDestinations = MutableStateFlow<List<RecentDestinationEntity>>(emptyList())
    val recentDestinations: StateFlow<List<RecentDestinationEntity>> = _recentDestinations.asStateFlow()

    private val _nearbyPois = MutableStateFlow<List<PoiItem>>(emptyList())
    val nearbyPois: StateFlow<List<PoiItem>> = _nearbyPois.asStateFlow()

    private val _selectedRouteType = MutableStateFlow(RouteType.FASTEST)
    val selectedRouteType: StateFlow<RouteType> = _selectedRouteType.asStateFlow()

    private var simulationJob: Job? = null
    private var pendingDestination: PoiItem? = null

    init {
        // Collect recent destinations
        viewModelScope.launch {
            destinationDao.getAllRecent().collect { list ->
                _recentDestinations.value = list
            }
        }

        // Initialize nearby POIs
        _nearbyPois.value = repository.getNearbyPois(PoiCategory.GAS, _currentLocation.value)

        // Seed default Home / Work if empty
        viewModelScope.launch {
            val existing = destinationDao.getAllRecent().firstOrNull()
            if (existing.isNullOrEmpty()) {
                destinationDao.insert(
                    RecentDestinationEntity(
                        name = "Home Residence",
                        address = "742 Evergreen Terrace, Metro Area",
                        latitude = 37.7650,
                        longitude = -122.4450,
                        category = "HOME"
                    )
                )
                destinationDao.insert(
                    RecentDestinationEntity(
                        name = "Work Office & Tech Park",
                        address = "500 Howard Street, Financial District",
                        latitude = 37.7885,
                        longitude = -122.3990,
                        category = "WORK"
                    )
                )
            }
        }

        // Search initial default POIs
        search("")

        // Start GPS updates
        locationHelper.startLocationUpdates { loc ->
            if (!_isSimulating.value) {
                _currentLocation.value = GeoPoint(loc.latitude, loc.longitude)
                if (loc.hasSpeed()) {
                    _currentSpeedKmh.value = loc.speed * 3.6f
                }
                if (loc.hasBearing()) {
                    _currentHeading.value = loc.bearing
                }
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            val results = repository.searchLocations(query, _currentLocation.value)
            _searchResults.value = results
        }
    }

    fun selectPoiCategory(category: PoiCategory) {
        val pois = repository.getNearbyPois(category, _currentLocation.value)
        _nearbyPois.value = pois
        val closest = pois.firstOrNull()
        if (closest != null) {
            selectDestination(closest)
        }
    }

    fun selectDestination(poi: PoiItem) {
        pendingDestination = poi
        _showSearchDialog.value = false
        val route = repository.calculateRoute(_currentLocation.value, poi, _selectedRouteType.value)
        _activeRoute.value = route
        _remainingDistanceMeters.value = route.totalDistanceMeters
        _drivingAlerts.value = repository.getUpcomingAlerts(route)
        updateEta(route.totalDurationSeconds)
        _showTripOverview.value = true

        // Save to Room recent history
        viewModelScope.launch {
            destinationDao.insert(
                RecentDestinationEntity(
                    name = poi.name,
                    address = poi.address,
                    latitude = poi.location.lat,
                    longitude = poi.location.lon,
                    category = "RECENT"
                )
            )
        }
    }

    fun selectRecentDestination(recent: RecentDestinationEntity) {
        val poi = PoiItem(
            id = "rec_${recent.id}",
            name = recent.name,
            category = PoiCategory.REST_STOP,
            location = GeoPoint(recent.latitude, recent.longitude),
            address = recent.address,
            distanceKm = 0.0,
            extraInfo = "Saved Destination"
        )
        selectDestination(poi)
    }

    fun selectRouteType(type: RouteType) {
        _selectedRouteType.value = type
        pendingDestination?.let { poi ->
            val route = repository.calculateRoute(_currentLocation.value, poi, type)
            _activeRoute.value = route
            _remainingDistanceMeters.value = route.totalDistanceMeters
            updateEta(route.totalDurationSeconds)
        }
    }

    fun startNavigation() {
        _showTripOverview.value = false
        _isLockedToCar.value = true
        val route = _activeRoute.value ?: return

        voiceService.speak("Starting navigation to ${route.destinationName}. Follow the highlighted route.", isPriority = true)

        val firstStep = route.steps.firstOrNull()
        if (firstStep != null) {
            _distanceToNextManeuverMeters.value = firstStep.distanceMeters
            voiceService.speak(firstStep.instruction)
        }
    }

    fun startDriveSimulation() {
        val route = _activeRoute.value ?: return
        _showTripOverview.value = false
        _isSimulating.value = true
        _isLockedToCar.value = true

        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            voiceService.speak("Drive simulation active. Approaching route.", isPriority = true)
            val polyline = route.polyline
            val steps = route.steps
            var stepIdx = 0

            for (i in 0 until polyline.size - 1) {
                if (!isActive) break

                val from = polyline[i]
                val to = polyline[i + 1]
                val segmentBearing = from.bearingTo(to)

                _currentLocation.value = from
                _currentHeading.value = segmentBearing

                // Vary speed realistically between 55 and 75 km/h
                val simulatedSpeed = 58f + (kotlin.math.sin(i * 0.4) * 12f).toFloat()
                _currentSpeedKmh.value = simulatedSpeed
                _speedLimitKmh.value = if (i > 15 && i < 45) 80 else 65

                // Update remaining distance
                val progressFraction = i.toFloat() / polyline.size
                _remainingDistanceMeters.value = (route.totalDistanceMeters * (1f - progressFraction)).toInt()

                // Calculate progress along steps
                val stepThreshold = (polyline.size / steps.size.coerceAtLeast(1))
                val targetStepIdx = (i / stepThreshold).coerceIn(0, steps.size - 1)

                if (targetStepIdx != stepIdx) {
                    stepIdx = targetStepIdx
                    _currentStepIndex.value = stepIdx
                    val step = steps[stepIdx]
                    voiceService.speak(step.instruction, isPriority = true)
                }

                val currentStep = steps.getOrNull(stepIdx)
                val distToStep = currentStep?.location?.let { from.distanceToMeters(it).toInt() } ?: 250
                _distanceToNextManeuverMeters.value = distToStep

                // Update ETA
                val remainingSec = (route.totalDurationSeconds * (1f - progressFraction)).toInt()
                updateEta(remainingSec)

                delay(600L) // 600ms per step creates a smooth drive simulation
            }

            // Arrive
            _currentSpeedKmh.value = 0f
            voiceService.speak("You have arrived at ${route.destinationName}.", isPriority = true)
            _isSimulating.value = false
        }
    }

    fun stopNavigation() {
        simulationJob?.cancel()
        simulationJob = null
        _isSimulating.value = false
        _activeRoute.value = null
        _currentStepIndex.value = 0
        _currentSpeedKmh.value = 0f
        voiceService.speak("Navigation stopped.")
    }

    private fun updateEta(durationSeconds: Int) {
        val arrivalMillis = System.currentTimeMillis() + (durationSeconds * 1000L)
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        _etaString.value = sdf.format(Date(arrivalMillis))
    }

    fun toggleSpeedUnit() {
        _speedUnit.value = if (_speedUnit.value == SpeedUnit.KMH) SpeedUnit.MPH else SpeedUnit.KMH
    }

    fun toggleVoice() {
        val newMuted = !_isVoiceMuted.value
        _isVoiceMuted.value = newMuted
        voiceService.isMuted = newMuted
        if (!newMuted) {
            voiceService.speak("Voice guidance enabled", isPriority = true)
        }
    }

    fun toggleHud() {
        _isHudActive.value = !_isHudActive.value
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
    }

    fun setKeepScreenAwake(enabled: Boolean) {
        _keepScreenAwake.value = enabled
    }

    fun recenterCar() {
        _isLockedToCar.value = true
    }

    fun onMapInteracted() {
        _isLockedToCar.value = false
    }

    fun openSearch() {
        _showSearchDialog.value = true
    }

    fun closeSearch() {
        _showSearchDialog.value = false
    }

    fun closeTripOverview() {
        _showTripOverview.value = false
    }

    fun openSettings() {
        _showSettingsDialog.value = true
    }

    fun closeSettings() {
        _showSettingsDialog.value = false
    }

    fun openSos() {
        _showSosDialog.value = true
    }

    fun closeSos() {
        _showSosDialog.value = false
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
        locationHelper.stopLocationUpdates()
        voiceService.shutdown()
    }
}
