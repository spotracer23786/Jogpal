package com.jogpal.app.core.common

import kotlin.math.*

object LocationUtils {
    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    fun encodeGeohash(latitude: Double, longitude: Double, precision: Int = 9): String {
        var latMin = -90.0
        var latMax = 90.0
        var lonMin = -180.0
        var lonMax = 180.0
        
        val geohash = StringBuilder()
        var bit = 0
        var ch = 0
        
        while (geohash.length < precision) {
            val mid: Double
            if (bit % 2 == 0) { // longitude
                mid = (lonMin + lonMax) / 2
                if (longitude > mid) {
                    ch = ch or (1 shl (4 - (bit % 5)))
                    lonMin = mid
                } else {
                    lonMax = mid
                }
            } else { // latitude
                mid = (latMin + latMax) / 2
                if (latitude > mid) {
                    ch = ch or (1 shl (4 - (bit % 5)))
                    latMin = mid
                } else {
                    latMax = mid
                }
            }
            
            bit++
            if (bit % 5 == 0) {
                geohash.append(BASE32[ch])
                ch = 0
            }
        }
        return geohash.toString()
    }

    /**
     * Decodes a geohash into its center latitude and longitude.
     */
    fun decodeGeohash(geohash: String): Pair<Double, Double> {
        var latMin = -90.0
        var latMax = 90.0
        var lonMin = -180.0
        var lonMax = 180.0
        
        var isEven = true
        for (char in geohash) {
            val value = BASE32.indexOf(char)
            for (i in 4 downTo 0) {
                val bit = (value shr i) and 1
                if (isEven) {
                    val mid = (lonMin + lonMax) / 2
                    if (bit == 1) lonMin = mid else lonMax = mid
                } else {
                    val mid = (latMin + latMax) / 2
                    if (bit == 1) latMin = mid else latMax = mid
                }
                isEven = !isEven
            }
        }
        return Pair((latMin + latMax) / 2, (lonMin + lonMax) / 2)
    }

    /**
     * Calculates distance between two points in kilometers using Haversine formula.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
