package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.example.model.GeoPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationHelper(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow(GeoPoint(37.7749, -122.4194))
    val currentLocation: StateFlow<GeoPoint> = _currentLocation.asStateFlow()

    private val _currentSpeedKmh = MutableStateFlow(0f)
    val currentSpeedKmh: StateFlow<Float> = _currentSpeedKmh.asStateFlow()

    private val _currentBearing = MutableStateFlow(0f)
    val currentBearing: StateFlow<Float> = _currentBearing.asStateFlow()

    private val _hasGpsFix = MutableStateFlow(false)
    val hasGpsFix: StateFlow<Boolean> = _hasGpsFix.asStateFlow()

    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(onLocationChanged: ((Location) -> Unit)? = null) {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(500L)
                .setMinUpdateDistanceMeters(1f)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val last = result.lastLocation ?: return
                    _hasGpsFix.value = true
                    _currentLocation.value = GeoPoint(last.latitude, last.longitude)

                    if (last.hasSpeed()) {
                        // speed is in m/s, convert to km/h (1 m/s = 3.6 km/h)
                        _currentSpeedKmh.value = last.speed * 3.6f
                    }
                    if (last.hasBearing()) {
                        _currentBearing.value = last.bearing
                    }
                    onLocationChanged?.invoke(last)
                }
            }

            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            _hasGpsFix.value = false
        } catch (_: Exception) {
            _hasGpsFix.value = false
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    fun updateManualLocation(point: GeoPoint, speedKmh: Float, bearing: Float) {
        _currentLocation.value = point
        _currentSpeedKmh.value = speedKmh
        _currentBearing.value = bearing
    }
}
