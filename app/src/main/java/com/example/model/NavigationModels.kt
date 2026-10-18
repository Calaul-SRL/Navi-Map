package com.example.model

data class GeoPoint(
    val lat: Double,
    val lon: Double
) {
    fun distanceToMeters(other: GeoPoint): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(other.lat - this.lat)
        val dLon = Math.toRadians(other.lon - this.lon)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(other.lat)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    fun bearingTo(other: GeoPoint): Float {
        val lat1 = Math.toRadians(this.lat)
        val lon1 = Math.toRadians(this.lon)
        val lat2 = Math.toRadians(other.lat)
        val lon2 = Math.toRadians(other.lon)

        val y = Math.sin(lon2 - lon1) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)
        val bearing = Math.toDegrees(Math.atan2(y, x))
        return ((bearing + 360) % 360).toFloat()
    }
}

enum class ManeuverType {
    DEPART,
    STRAIGHT,
    TURN_SLIGHT_LEFT,
    TURN_LEFT,
    TURN_SHARP_LEFT,
    TURN_SLIGHT_RIGHT,
    TURN_RIGHT,
    TURN_SHARP_RIGHT,
    U_TURN,
    ROUNDABOUT_ENTER,
    ROUNDABOUT_EXIT,
    MERGE,
    HIGHWAY_ENTER,
    HIGHWAY_EXIT,
    ARRIVE
}

data class LaneInfo(
    val laneIndex: Int,
    val isRecommended: Boolean,
    val directionArrow: String // "straight", "left", "right", "slight_left", "slight_right"
)

data class RouteStep(
    val id: String,
    val instruction: String,
    val maneuver: ManeuverType,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val streetName: String,
    val location: GeoPoint,
    val lanes: List<LaneInfo> = emptyList()
)

enum class RouteType {
    FASTEST,
    SHORTEST,
    ECO_FRIENDLY,
    AVOID_TOLLS
}

data class NavRoute(
    val id: String,
    val title: String,
    val destinationName: String,
    val destinationAddress: String,
    val totalDistanceMeters: Int,
    val totalDurationSeconds: Int,
    val polyline: List<GeoPoint>,
    val steps: List<RouteStep>,
    val tollFree: Boolean = true,
    val routeType: RouteType = RouteType.FASTEST,
    val trafficDelayMinutes: Int = 0
)

enum class PoiCategory(val label: String) {
    GAS("Gas & Fuel"),
    EV_CHARGING("EV Charging"),
    PARKING("Parking"),
    FOOD("Food & Drink"),
    REST_STOP("Rest Area"),
    HOSPITAL("Emergency Care")
}

data class PoiItem(
    val id: String,
    val name: String,
    val category: PoiCategory,
    val location: GeoPoint,
    val address: String,
    val distanceKm: Double,
    val extraInfo: String, // e.g. "$3.45/gal", "150kW • 4 Free", "32 spots free"
    val rating: Float = 4.5f
)

enum class SpeedUnit(val label: String, val multiplier: Float) {
    KMH("km/h", 1.0f),
    MPH("mph", 0.621371f)
}

enum class AlertType(val title: String) {
    SPEED_CAMERA("Speed Camera"),
    HAZARD("Road Hazard"),
    POLICE("Police Reported"),
    CONSTRUCTION("Road Work"),
    CONGESTION("Heavy Traffic Ahead")
}

data class DrivingAlert(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: AlertType,
    val distanceAheadMeters: Int
)

data class DriveStats(
    val currentSpeedKmh: Float = 0f,
    val speedLimitKmh: Int = 65,
    val maxSpeedKmh: Float = 0f,
    val avgSpeedKmh: Float = 0f,
    val distanceDrivenMeters: Float = 0f,
    val driveDurationSeconds: Long = 0L,
    val headingDegrees: Float = 0f,
    val altitudeMeters: Double = 42.0
)
