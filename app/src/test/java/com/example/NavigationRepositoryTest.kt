package com.example

import com.example.data.repository.NavigationRepository
import com.example.model.GeoPoint
import com.example.model.PoiCategory
import com.example.model.RouteType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationRepositoryTest {

    private val repository = NavigationRepository()

    @Test
    fun testGeoDistanceCalculation() {
        val start = GeoPoint(37.7749, -122.4194)
        val end = GeoPoint(37.8080, -122.4155)
        val distMeters = start.distanceToMeters(end)
        assertTrue("Distance should be greater than 3000m", distMeters > 3000)
    }

    @Test
    fun testRouteCalculationHasStepsAndPolyline() {
        val start = repository.defaultStartLocation
        val pois = repository.getNearbyPois(PoiCategory.GAS, start)
        assertTrue("Should have nearby gas stations", pois.isNotEmpty())

        val target = pois.first()
        val route = repository.calculateRoute(start, target, RouteType.FASTEST)

        assertNotNull(route)
        assertTrue("Polyline should have points", route.polyline.isNotEmpty())
        assertTrue("Steps should have maneuvers", route.steps.isNotEmpty())
        assertTrue("Distance should be positive", route.totalDistanceMeters > 0)
    }

    @Test
    fun testSearchCuratedFallback() = runBlocking {
        val results = repository.searchLocations("Airport")
        assertTrue("Should find airport in search", results.any { it.name.contains("Airport", ignoreCase = true) })
    }
}
