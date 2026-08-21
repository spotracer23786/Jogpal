package com.jogpal.app.domain.run

data class RouteResult(
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val distanceKm: Double,
    val durationMinutes: Int,
    val encodedPolyline: String,
    val destinationName: String = ""
)
