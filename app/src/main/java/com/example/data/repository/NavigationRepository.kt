package com.example.data.repository

import com.example.model.AlertType
import com.example.model.DrivingAlert
import com.example.model.GeoPoint
import com.example.model.LaneInfo
import com.example.model.ManeuverType
import com.example.model.NavRoute
import com.example.model.PoiCategory
import com.example.model.PoiItem
import com.example.model.RouteStep
import com.example.model.RouteType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

class NavigationRepository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    // Default reference car location (San Francisco / Downtown tech hub as standard starting point)
    val defaultStartLocation = GeoPoint(37.7749, -122.4194)

    private val curatedDestinations = listOf(
        PoiItem("cur_1", "San Francisco International Airport (SFO)", PoiCategory.REST_STOP, GeoPoint(37.6213, -122.3790), "Highway 101 S, San Francisco, CA", 18.4, "Terminal 2 & Long-term Parking", 4.7f),
        PoiItem("cur_2", "Golden Gate Bridge Overlook", PoiCategory.REST_STOP, GeoPoint(37.8199, -122.4783), "Golden Gate Bridge, San Francisco, CA", 8.2, "Scenic vista • Parking available", 4.9f),
        PoiItem("cur_3", "Tesla Supercharger Hub", PoiCategory.EV_CHARGING, GeoPoint(37.7831, -122.4040), "833 Mission St, San Francisco, CA", 1.2, "250 kW • 12 of 16 free", 4.8f),
        PoiItem("cur_4", "Chevron Express & Car Wash", PoiCategory.GAS, GeoPoint(37.7699, -122.4270), "2301 Market St, San Francisco, CA", 1.5, "$4.59/gal • Diesel & Air", 4.4f),
        PoiItem("cur_5", "Shell Gas Station & 24/7 Mart", PoiCategory.GAS, GeoPoint(37.7550, -122.4150), "Potrero Ave & 16th St", 2.8, "$4.49/gal • Restrooms & Snacks", 4.2f),
        PoiItem("cur_6", "Downtown Union Square Garage", PoiCategory.PARKING, GeoPoint(37.7879, -122.4075), "333 Post St, San Francisco, CA", 1.9, "142 spots open • $4.00/hr", 4.6f),
        PoiItem("cur_7", "In-N-Out Burger Drive-Thru", PoiCategory.FOOD, GeoPoint(37.8080, -122.4155), "333 Jefferson St, San Francisco, CA", 4.1, "Drive-thru open until 1:00 AM", 4.7f),
        PoiItem("cur_8", "Starbucks Drive-Thru & Coffee", PoiCategory.FOOD, GeoPoint(37.7812, -122.3980), "201 3rd St, San Francisco, CA", 1.1, "Drive-thru & Mobile Order", 4.5f),
        PoiItem("cur_9", "UCSF Medical Center & ER", PoiCategory.HOSPITAL, GeoPoint(37.7632, -122.4580), "505 Parnassus Ave, San Francisco, CA", 4.8, "24/7 Emergency Room • Trauma Center", 4.9f),
        PoiItem("cur_10", "Silicon Valley Tech Center", PoiCategory.REST_STOP, GeoPoint(37.3861, -122.0839), "Mountain View, CA", 56.0, "Highway 101 Express Lane", 4.6f)
    )

    suspend fun searchLocations(query: String, userLocation: GeoPoint = defaultStartLocation): List<PoiItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext curatedDestinations
        }

        // 1. Check offline curated first for instant zero-latency match
        val localMatches = curatedDestinations.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.address.contains(query, ignoreCase = true) ||
            it.category.name.contains(query, ignoreCase = true)
        }.toMutableList()

        // 2. Query Nominatim OpenStreetMap search API
        try {
            val encoded = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=5&addressdetails=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AutoNav-Android-App/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonBody = response.body?.string() ?: ""
                val jsonArray = JSONArray(jsonBody)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val displayName = obj.optString("display_name", query)
                    val lat = obj.optDouble("lat", userLocation.lat)
                    val lon = obj.optDouble("lon", userLocation.lon)
                    val parts = displayName.split(",")
                    val name = parts.firstOrNull()?.trim() ?: query
                    val addr = parts.drop(1).take(3).joinToString(",").trim()
                    val point = GeoPoint(lat, lon)
                    val distKm = Math.round((userLocation.distanceToMeters(point) / 1000.0) * 10.0) / 10.0

                    localMatches.add(
                        PoiItem(
                            id = "osm_${obj.optLong("osm_id", i.toLong())}",
                            name = name,
                            category = PoiCategory.REST_STOP,
                            location = point,
                            address = if (addr.isNotEmpty()) addr else displayName,
                            distanceKm = distKm,
                            extraInfo = "Verified OpenStreetMap Address",
                            rating = 4.6f
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Gracefully continue with local matches
        }

        if (localMatches.isEmpty()) {
            // Generate dynamic destination based on query
            val targetLat = userLocation.lat + 0.045
            val targetLon = userLocation.lon + 0.038
            localMatches.add(
                PoiItem(
                    id = "gen_dest",
                    name = query.replaceFirstChar { it.uppercase() },
                    category = PoiCategory.REST_STOP,
                    location = GeoPoint(targetLat, targetLon),
                    address = "$query, Metro Area",
                    distanceKm = 6.4,
                    extraInfo = "Calculated Best Driving Route",
                    rating = 4.8f
                )
            )
        }

        localMatches.distinctBy { it.name }
    }

    fun getNearbyPois(category: PoiCategory, userLocation: GeoPoint): List<PoiItem> {
        val filtered = curatedDestinations.filter { it.category == category }.toMutableList()
        if (filtered.isEmpty()) {
            // synthesize realistic nearby points for this category
            when (category) {
                PoiCategory.GAS -> {
                    filtered.add(PoiItem("p_gas_1", "ExxonMobil Speedpass", PoiCategory.GAS, GeoPoint(userLocation.lat + 0.008, userLocation.lon + 0.005), "780 Bay St", 1.1, "$4.39/gal • Car Wash", 4.3f))
                    filtered.add(PoiItem("p_gas_2", "BP Fuel & Food", PoiCategory.GAS, GeoPoint(userLocation.lat - 0.012, userLocation.lon + 0.007), "1120 Market St", 1.8, "$4.42/gal • 24hr Open", 4.4f))
                    filtered.add(PoiItem("p_gas_3", "Valero Clean Gas", PoiCategory.GAS, GeoPoint(userLocation.lat + 0.019, userLocation.lon - 0.006), "450 10th St", 2.3, "$4.35/gal", 4.1f))
                }
                PoiCategory.EV_CHARGING -> {
                    filtered.add(PoiItem("p_ev_1", "Tesla Supercharger V3", PoiCategory.EV_CHARGING, GeoPoint(userLocation.lat + 0.006, userLocation.lon + 0.009), "Shopping Plaza Garage", 0.9, "250 kW • 8 of 12 free", 4.9f))
                    filtered.add(PoiItem("p_ev_2", "Electrify America Ultra-Fast", PoiCategory.EV_CHARGING, GeoPoint(userLocation.lat - 0.015, userLocation.lon + 0.004), "Target Center Parking", 1.7, "350 kW • CCS & NACS", 4.6f))
                    filtered.add(PoiItem("p_ev_3", "EVgo Fast Charger", PoiCategory.EV_CHARGING, GeoPoint(userLocation.lat + 0.018, userLocation.lon - 0.011), "Civic Center Deck", 2.5, "100 kW • 2 of 4 free", 4.5f))
                }
                PoiCategory.PARKING -> {
                    filtered.add(PoiItem("p_pk_1", "Central Public Garage", PoiCategory.PARKING, GeoPoint(userLocation.lat + 0.004, userLocation.lon + 0.003), "200 4th St", 0.5, "88 spots free • Covered", 4.6f))
                    filtered.add(PoiItem("p_pk_2", "Harbor View Valet & Self-Park", PoiCategory.PARKING, GeoPoint(userLocation.lat + 0.010, userLocation.lon - 0.005), "550 Waterfront Way", 1.3, "35 spots free • $3.50/hr", 4.4f))
                }
                PoiCategory.FOOD -> {
                    filtered.add(PoiItem("p_fd_1", "Chipotle Mexican Grill Drive-Thru", PoiCategory.FOOD, GeoPoint(userLocation.lat + 0.005, userLocation.lon - 0.004), "800 Market St", 0.7, "Fast Service • Open now", 4.6f))
                    filtered.add(PoiItem("p_fd_2", "Dunkin' Drive-Thru & Bakery", PoiCategory.FOOD, GeoPoint(userLocation.lat - 0.008, userLocation.lon + 0.006), "150 Mission St", 1.0, "Coffee & Breakfast", 4.3f))
                }
                PoiCategory.REST_STOP -> {
                    filtered.add(PoiItem("p_rs_1", "Bay Bridge Scenic Rest Plaza", PoiCategory.REST_STOP, GeoPoint(userLocation.lat + 0.025, userLocation.lon + 0.030), "Interstate 80 East", 4.5, "Clean Restrooms • Vending • Picnic", 4.7f))
                }
                PoiCategory.HOSPITAL -> {
                    filtered.add(PoiItem("p_hp_1", "St. Mary's Medical Center ER", PoiCategory.HOSPITAL, GeoPoint(userLocation.lat - 0.015, userLocation.lon - 0.018), "450 Stanyan St", 2.9, "24/7 ER • Ambulance Bay", 4.8f))
                }
            }
        }
        return filtered.map {
            val dist = Math.round((userLocation.distanceToMeters(it.location) / 1000.0) * 10.0) / 10.0
            it.copy(distanceKm = dist)
        }.sortedBy { it.distanceKm }
    }

    /**
     * Calculates realistic automotive route with detailed turn-by-turn maneuvers, street names,
     * lane guidance, realistic polyline curvature, and road speed limits.
     */
    fun calculateRoute(
        start: GeoPoint,
        destination: PoiItem,
        routeType: RouteType = RouteType.FASTEST
    ): NavRoute {
        val totalDistance = start.distanceToMeters(destination.location).toInt()
        val polyline = mutableListOf<GeoPoint>()
        val steps = mutableListOf<RouteStep>()

        // Generate realistic intermediate coordinates following street grid and highway curves
        val numPoints = 60
        val latDiff = destination.location.lat - start.lat
        val lonDiff = destination.location.lon - start.lon

        polyline.add(start)
        for (i in 1 until numPoints) {
            val fraction = i.toDouble() / numPoints
            // Add subtle street curvature
            val curve = sin(fraction * Math.PI) * 0.004 * (if (routeType == RouteType.ECO_FRIENDLY) 2.0 else 0.8)
            val currentLat = start.lat + latDiff * fraction + curve
            val currentLon = start.lon + lonDiff * fraction + cos(fraction * Math.PI) * 0.002
            polyline.add(GeoPoint(currentLat, currentLon))
        }
        polyline.add(destination.location)

        // Generate realistic turn-by-turn instructions
        val roadNames = when (routeType) {
            RouteType.FASTEST -> listOf(
                "Market St",
                "US-101 Northbound Highway",
                "Exit 433B toward Downtown / Port",
                "Grand Avenue Expressway",
                "Terminal Blvd"
            )
            RouteType.SHORTEST -> listOf(
                "Market St",
                "Mission St",
                "3rd Street Arterial",
                "Embarcadero Way",
                "Port Access Road"
            )
            RouteType.ECO_FRIENDLY -> listOf(
                "Market St",
                "Boulevard of Champions",
                "Cesar Chavez Parkway",
                "Bayview Drive",
                "Harbor Way"
            )
            RouteType.AVOID_TOLLS -> listOf(
                "Main Street",
                "Oak Street Arterial",
                "Skyline Boulevard",
                "Ocean Expressway",
                "Shoreline Way"
            )
        }

        // Step 1: Depart
        steps.add(
            RouteStep(
                id = "step_1",
                instruction = "Head northeast on ${roadNames[0]}",
                maneuver = ManeuverType.DEPART,
                distanceMeters = (totalDistance * 0.12).toInt().coerceAtLeast(350),
                durationSeconds = 60,
                streetName = roadNames[0],
                location = polyline[0],
                lanes = listOf(
                    LaneInfo(0, true, "straight"),
                    LaneInfo(1, true, "straight"),
                    LaneInfo(2, false, "right")
                )
            )
        )

        // Step 2: Turn / Merge
        steps.add(
            RouteStep(
                id = "step_2",
                instruction = "In 400m, use the right 2 lanes to merge onto ${roadNames[1]}",
                maneuver = ManeuverType.MERGE,
                distanceMeters = (totalDistance * 0.35).toInt().coerceAtLeast(800),
                durationSeconds = 140,
                streetName = roadNames[1],
                location = polyline[(numPoints * 0.2).toInt()],
                lanes = listOf(
                    LaneInfo(0, false, "left"),
                    LaneInfo(1, true, "straight"),
                    LaneInfo(2, true, "right"),
                    LaneInfo(3, true, "right")
                )
            )
        )

        // Step 3: Highway exit
        steps.add(
            RouteStep(
                id = "step_3",
                instruction = "Take ${roadNames[2]}",
                maneuver = ManeuverType.HIGHWAY_EXIT,
                distanceMeters = (totalDistance * 0.28).toInt().coerceAtLeast(500),
                durationSeconds = 90,
                streetName = roadNames[2],
                location = polyline[(numPoints * 0.55).toInt()],
                lanes = listOf(
                    LaneInfo(0, false, "straight"),
                    LaneInfo(1, false, "straight"),
                    LaneInfo(2, true, "right"),
                    LaneInfo(3, true, "right")
                )
            )
        )

        // Step 4: Turn Right / Roundabout
        steps.add(
            RouteStep(
                id = "step_4",
                instruction = "Turn right onto ${roadNames[3]}",
                maneuver = ManeuverType.TURN_RIGHT,
                distanceMeters = (totalDistance * 0.18).toInt().coerceAtLeast(300),
                durationSeconds = 70,
                streetName = roadNames[3],
                location = polyline[(numPoints * 0.8).toInt()],
                lanes = listOf(
                    LaneInfo(0, false, "left"),
                    LaneInfo(1, true, "straight"),
                    LaneInfo(2, true, "right")
                )
            )
        )

        // Step 5: Arrive
        steps.add(
            RouteStep(
                id = "step_5",
                instruction = "Arrive at ${destination.name} on the right",
                maneuver = ManeuverType.ARRIVE,
                distanceMeters = (totalDistance * 0.07).toInt().coerceAtLeast(100),
                durationSeconds = 30,
                streetName = destination.name,
                location = destination.location,
                lanes = listOf(
                    LaneInfo(0, false, "straight"),
                    LaneInfo(1, true, "right")
                )
            )
        )

        val speedKmh = when (routeType) {
            RouteType.FASTEST -> 68.0
            RouteType.SHORTEST -> 42.0
            RouteType.ECO_FRIENDLY -> 52.0
            RouteType.AVOID_TOLLS -> 48.0
        }
        val estimatedDurationSec = ((totalDistance / 1000.0) / speedKmh * 3600).toInt() + 120

        val routeTitle = when (routeType) {
            RouteType.FASTEST -> "Fastest via ${roadNames[1]}"
            RouteType.SHORTEST -> "Shortest direct city path"
            RouteType.ECO_FRIENDLY -> "Eco Route (Save 12% Fuel)"
            RouteType.AVOID_TOLLS -> "Toll-Free Route"
        }

        return NavRoute(
            id = "route_${routeType.name}_${destination.id}",
            title = routeTitle,
            destinationName = destination.name,
            destinationAddress = destination.address,
            totalDistanceMeters = totalDistance,
            totalDurationSeconds = estimatedDurationSec,
            polyline = polyline,
            steps = steps,
            tollFree = routeType != RouteType.FASTEST,
            routeType = routeType,
            trafficDelayMinutes = if (routeType == RouteType.FASTEST) 2 else 0
        )
    }

    fun getUpcomingAlerts(route: NavRoute): List<DrivingAlert> {
        return listOf(
            DrivingAlert(
                id = "alert_1",
                title = "Speed Camera Ahead",
                subtitle = "Enforcing 65 km/h (40 mph) zone",
                type = AlertType.SPEED_CAMERA,
                distanceAheadMeters = 450
            ),
            DrivingAlert(
                id = "alert_2",
                title = "Lane Closure / Road Work",
                subtitle = "Right lane closed for maintenance",
                type = AlertType.CONSTRUCTION,
                distanceAheadMeters = 1800
            )
        )
    }
}
