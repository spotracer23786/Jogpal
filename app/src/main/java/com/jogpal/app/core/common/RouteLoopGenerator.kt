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
        val earthRadius = 6371.0
        val latRadius = Math.toDegrees(radiusKm / earthRadius)
        
        // Pick a heading direction
        val headingAngle = Math.toRadians((0..359).random().toDouble())
        
        // Center coordinates of the loop offset from starting point
        val centerLat = startLat + latRadius * cos(headingAngle)
        val centerLng = startLng + (latRadius * sin(headingAngle)) / cos(Math.toRadians(startLat))

        val steps = 16
        for (i in 0..steps) {
            val angle = headingAngle + Math.PI + (2 * Math.PI * i / steps)
            val pointLat = centerLat + latRadius * cos(angle)
            val pointLng = centerLng + (latRadius * sin(angle)) / cos(Math.toRadians(centerLat))
            points.add(LatLng(pointLat, pointLng))
        }

        return points
    }

    /**
     * Generates an out-and-back route: goes straight for half the distance and returns.
     */
    fun generateOutAndBack(startLat: Double, startLng: Double, totalDistanceKm: Double): List<LatLng> {
        if (totalDistanceKm <= 0) return emptyList()
        
        val points = mutableListOf<LatLng>()
        val oneWayDist = totalDistanceKm / 2.0
        val earthRadius = 6371.0
        
        // Random direction for the out-and-back run
        val angle = Math.toRadians((0..359).random().toDouble())
        val deltaLat = Math.toDegrees((oneWayDist / earthRadius) * cos(angle))
        val deltaLng = Math.toDegrees(((oneWayDist / earthRadius) * sin(angle)) / cos(Math.toRadians(startLat)))
        
        val endLat = startLat + deltaLat
        val endLng = startLng + deltaLng
        
        // Interpolate outbound
        val steps = 8
        for (i in 0..steps) {
            val fraction = i.toDouble() / steps
            points.add(LatLng(startLat + deltaLat * fraction, startLng + deltaLng * fraction))
        }
        // Interpolate inbound (slightly offset to not perfectly overlap if desired, or exact match)
        for (i in 1..steps) {
            val fraction = 1.0 - (i.toDouble() / steps)
            // Tiny offset so they see both lanes
            val offsetFactor = 0.00002 * sin(i * Math.PI / steps)
            points.add(LatLng(startLat + deltaLat * fraction + offsetFactor, startLng + deltaLng * fraction + offsetFactor))
        }
        
        return points
    }

    /**
     * Generates a random trail: a zig-zag style path that leads back to start.
     */
    fun generateRandomTrail(startLat: Double, startLng: Double, totalDistanceKm: Double): List<LatLng> {
        if (totalDistanceKm <= 0) return emptyList()
        
        val points = mutableListOf<LatLng>()
        points.add(LatLng(startLat, startLng))
        
        val steps = 10
        val stepDist = totalDistanceKm / steps
        val earthRadius = 6371.0
        
        var currentLat = startLat
        var currentLng = startLng
        var baseAngle = (0..359).random().toDouble()
        
        for (i in 1 until steps) {
            // Add some zig-zag deviation
            val deviation = (-45..45).random().toDouble()
            val angleRad = Math.toRadians(baseAngle + deviation)
            
            val deltaLat = Math.toDegrees((stepDist / earthRadius) * cos(angleRad))
            val deltaLng = Math.toDegrees(((stepDist / earthRadius) * sin(angleRad)) / cos(Math.toRadians(currentLat)))
            
            currentLat += deltaLat
            currentLng += deltaLng
            points.add(LatLng(currentLat, currentLng))
            
            // Gradually steer back towards the start on the latter half
            if (i >= steps / 2) {
                val dx = startLng - currentLng
                val dy = startLat - currentLat
                baseAngle = Math.toDegrees(Math.atan2(dx, dy))
            }
        }
        
        // Final step directly back to start
        points.add(LatLng(startLat, startLng))
        return points
    }
}

