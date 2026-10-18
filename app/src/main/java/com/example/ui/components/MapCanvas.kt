package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.model.AlertType
import com.example.model.DrivingAlert
import com.example.model.GeoPoint
import com.example.model.NavRoute
import com.example.model.PoiCategory
import com.example.model.PoiItem
import com.example.ui.theme.CategoryEvColor
import com.example.ui.theme.CategoryGasColor
import com.example.ui.theme.CategoryParkingColor
import com.example.ui.theme.NavGlowBlue
import com.example.ui.theme.NavRouteCyan
import com.example.ui.theme.SpeedDanger
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MapCanvas(
    carLocation: GeoPoint,
    carHeading: Float,
    activeRoute: NavRoute?,
    nearbyPois: List<PoiItem>,
    alerts: List<DrivingAlert>,
    isLockedToCar: Boolean,
    is3dPerspective: Boolean,
    onMapInteracted: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Zoom factor: degrees to canvas pixels
    var zoomScale by remember { mutableFloatStateOf(65000f) }
    // Manual pan offset when unlocked
    var manualPanOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("map_canvas_container")
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onMapInteracted()
                    zoomScale = (zoomScale * zoom).coerceIn(20000f, 250000f)
                    manualPanOffset += pan
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenter = Offset(size.width / 2f, size.height / 2f)

            // Calculate center reference point
            val centerLat: Double
            val centerLon: Double
            if (isLockedToCar) {
                centerLat = carLocation.lat
                centerLon = carLocation.lon
            } else {
                // Incorporate manual pan
                centerLat = carLocation.lat - (manualPanOffset.y / zoomScale)
                centerLon = carLocation.lon + (manualPanOffset.x / zoomScale)
            }

            fun geoToCanvas(point: GeoPoint): Offset {
                val dx = (point.lon - centerLon) * zoomScale
                val dy = -(point.lat - centerLat) * zoomScale
                return Offset(
                    x = canvasCenter.x + dx.toFloat(),
                    y = canvasCenter.y + dy.toFloat()
                )
            }

            // 1. Draw Automotive Dark Terrain / Night Sky Background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF090D16),
                        Color(0xFF0F172A),
                        Color(0xFF0B1120)
                    )
                )
            )

            // 2. Draw Decorative Water / Bay / Shoreline
            drawWaterBody(canvasCenter, size.width, size.height)

            // 3. Draw Street Grid & Urban Blocks
            drawCityRoadNetwork(canvasCenter, zoomScale, size.width, size.height)

            // 4. Draw Active Route Polyline (Glow + Solid Path + Direction arrows)
            if (activeRoute != null && activeRoute.polyline.isNotEmpty()) {
                val pathPoints = activeRoute.polyline.map { geoToCanvas(it) }

                // Outer Glowing Ambient Path
                val glowPath = Path().apply {
                    pathPoints.forEachIndexed { idx, pt ->
                        if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                    }
                }

                // Ambient glow stroke
                drawPath(
                    path = glowPath,
                    color = NavGlowBlue.copy(alpha = 0.25f),
                    style = Stroke(
                        width = 24f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Neon active route core
                drawPath(
                    path = glowPath,
                    color = NavRouteCyan,
                    style = Stroke(
                        width = 12f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Route Destination Finish Flag Pin
                val destPt = pathPoints.last()
                drawCircle(
                    color = Color.White,
                    radius = 16f,
                    center = destPt
                )
                drawCircle(
                    color = SpeedDanger,
                    radius = 12f,
                    center = destPt
                )
            }

            // 5. Draw Nearby POI Pins (Gas Stations, EV Chargers, Parking)
            nearbyPois.forEach { poi ->
                val poiCanvasPt = geoToCanvas(poi.location)
                if (poiCanvasPt.x in -50f..(size.width + 50f) && poiCanvasPt.y in -50f..(size.height + 50f)) {
                    val pinColor = when (poi.category) {
                        PoiCategory.GAS -> CategoryGasColor
                        PoiCategory.EV_CHARGING -> CategoryEvColor
                        PoiCategory.PARKING -> CategoryParkingColor
                        else -> Color(0xFFEAB308)
                    }
                    drawCircle(color = pinColor.copy(alpha = 0.35f), radius = 18f, center = poiCanvasPt)
                    drawCircle(color = Color.White, radius = 10f, center = poiCanvasPt)
                    drawCircle(color = pinColor, radius = 7f, center = poiCanvasPt)
                }
            }

            // 6. Draw Hazard / Speed Camera Radar Markers
            alerts.forEach { alert ->
                // Place along route roughly
                val alertOffset = Offset(canvasCenter.x + 40f, canvasCenter.y - 120f)
                drawCircle(color = SpeedDanger.copy(alpha = 0.4f), radius = 22f, center = alertOffset)
                drawCircle(color = SpeedDanger, radius = 10f, center = alertOffset)
            }

            // 7. Draw 3D Vehicle Pointer / Glowing Chevron Marker
            val carCanvasPt = geoToCanvas(carLocation)

            // Vehicle radar pulse rings
            drawCircle(
                color = NavRouteCyan.copy(alpha = 0.15f),
                radius = 42f,
                center = carCanvasPt
            )
            drawCircle(
                color = NavRouteCyan.copy(alpha = 0.3f),
                radius = 28f,
                center = carCanvasPt
            )

            // Vehicle Arrowhead oriented to vehicle heading
            rotate(degrees = carHeading, pivot = carCanvasPt) {
                val arrowPath = Path().apply {
                    moveTo(carCanvasPt.x, carCanvasPt.y - 24f) // Tip forward
                    lineTo(carCanvasPt.x + 16f, carCanvasPt.y + 16f) // Right rear
                    lineTo(carCanvasPt.x, carCanvasPt.y + 8f) // Indent
                    lineTo(carCanvasPt.x - 16f, carCanvasPt.y + 16f) // Left rear
                    close()
                }

                // White outline
                drawPath(
                    path = arrowPath,
                    color = Color.White,
                    style = Fill
                )
                // Cyan inner
                val innerArrow = Path().apply {
                    moveTo(carCanvasPt.x, carCanvasPt.y - 20f)
                    lineTo(carCanvasPt.x + 12f, carCanvasPt.y + 12f)
                    lineTo(carCanvasPt.x, carCanvasPt.y + 6f)
                    lineTo(carCanvasPt.x - 12f, carCanvasPt.y + 12f)
                    close()
                }
                drawPath(
                    path = innerArrow,
                    color = NavRouteCyan,
                    style = Fill
                )
            }
        }
    }
}

private fun DrawScope.drawWaterBody(center: Offset, width: Float, height: Float) {
    val waterPath = Path().apply {
        moveTo(width * 0.75f, 0f)
        cubicTo(
            width * 0.65f, height * 0.3f,
            width * 0.85f, height * 0.7f,
            width * 0.70f, height
        )
        lineTo(width, height)
        lineTo(width, 0f)
        close()
    }
    drawPath(
        path = waterPath,
        color = Color(0xFF082F49).copy(alpha = 0.65f)
    )
}

private fun DrawScope.drawCityRoadNetwork(
    center: Offset,
    scale: Float,
    width: Float,
    height: Float
) {
    val roadColorSecondary = Color(0xFF1E293B)
    val roadColorPrimary = Color(0xFF334155)
    val highwayColor = Color(0xFF475569)

    val gridSpacing = 80f

    // Horizontal secondary roads
    var y = (center.y % gridSpacing)
    while (y < height) {
        drawLine(
            color = roadColorSecondary,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 3f
        )
        y += gridSpacing
    }

    // Vertical secondary roads
    var x = (center.x % gridSpacing)
    while (x < width) {
        drawLine(
            color = roadColorSecondary,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 3f
        )
        x += gridSpacing
    }

    // Main Diagonal Highway Arterial
    drawLine(
        color = highwayColor,
        start = Offset(0f, height * 0.85f),
        end = Offset(width * 0.9f, 0f),
        strokeWidth = 8f
    )

    // Parallel Arterial Expressway
    drawLine(
        color = roadColorPrimary,
        start = Offset(width * 0.15f, height),
        end = Offset(width, height * 0.2f),
        strokeWidth = 6f
    )
}
