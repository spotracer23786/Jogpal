package com.jogpal.app.core.common

import org.maplibre.android.geometry.LatLng
import kotlin.math.cos
import kotlin.math.sin

object RouteLoopGenerator {

    /**
     * Generates a circular loop starting and ending at the user's location,
     * with a total perimeter roughly matching the target distance (km).
     */
    fun generateLoop(startLat: Double, startLng: Double, totalDistanceKm: Double): List<LatLng> {
        if (totalDistanceKm <= 0) return emptyList()

        val points = mutableListOf<LatLng>()
        
        // Approximate radius of the loop (circumference C = 2 * pi * r)
        val radiusKm = totalDistanceKm / (2 * Math.PI)
        
        // Earth radius in km
        val earthRadius = 6371.0
        
        // Convert radius to degrees (latitude delta)
        val latRadius = Math.toDegrees(radiusKm / earthRadius)
        
        // Pick a random heading direction for the loop center to keep things interesting
        val headingAngle = Math.toRadians((0..359).random().toDouble())
        
        // Center coordinates of the loop offset from starting point
        val centerLat = startLat + latRadius * cos(headingAngle)
        val centerLng = startLng + (latRadius * sin(headingAngle)) / cos(Math.toRadians(startLat))

        // Create circle points (e.g. 16 checkpoints) starting from user position
        val steps = 16
        for (i in 0..steps) {
            val angle = headingAngle + Math.PI + (2 * Math.PI * i / steps)
            val pointLat = centerLat + latRadius * cos(angle)
            val pointLng = centerLng + (latRadius * sin(angle)) / cos(Math.toRadians(centerLat))
            points.add(LatLng(pointLat, pointLng))
        }

        return points
    }
}
