package com.jogpal.app.domain.run

interface RouteRepository {
    suspend fun calculateRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        alternatives: Boolean = false
    ): Result<List<RouteResult>>
}
