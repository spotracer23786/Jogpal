package com.jogpal.app.domain.ghost

import com.jogpal.app.domain.model.GeoPoint

data class GhostRun(
    val id: String,
    val title: String,
    val dateFormatted: String,
    val distanceKm: Double,
    val durationSeconds: Long,
    val pace: String,
    val isPersonalBest: Boolean = false,
    val encodedPolyline: String? = null,
    val waypoints: List<GeoPoint> = emptyList()
) {
    val formattedTime: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
