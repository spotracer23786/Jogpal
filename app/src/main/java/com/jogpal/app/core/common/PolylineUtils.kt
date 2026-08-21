package com.jogpal.app.core.common

import com.jogpal.app.domain.model.GeoPoint

object PolylineUtils {

    /**
     * Decodes an encoded polyline string into a list of GeoPoint points.
     * Standard Google Maps polyline algorithm.
     */
    fun decodePolyline(encoded: String): List<GeoPoint> {
        val poly = ArrayList<GeoPoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = GeoPoint(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }

    /**
     * Encodes a list of GeoPoint points into a polyline string.
     */
    fun encodePolyline(points: List<GeoPoint>): String {
        val result = StringBuilder()

        var prevLat = 0
        var prevLng = 0

        for (point in points) {
            val lat = (point.lat * 1e5).toInt()
            val lng = (point.lng * 1e5).toInt()

            encodeValue(lat - prevLat, result)
            encodeValue(lng - prevLng, result)

            prevLat = lat
            prevLng = lng
        }

        return result.toString()
    }

    private fun encodeValue(value: Int, result: StringBuilder) {
        var v = if (value < 0) (value shl 1).inv() else value shl 1
        while (v >= 0x20) {
            result.append(((0x20 or (v and 0x1f)) + 63).toChar())
            v = v shr 5
        }
        result.append((v + 63).toChar())
    }
}
