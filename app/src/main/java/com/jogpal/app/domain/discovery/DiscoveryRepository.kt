package com.jogpal.app.domain.discovery

import com.jogpal.app.domain.user.UserProfile

interface DiscoveryRepository {
    suspend fun findNearbyRunners(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<UserProfile>>
}
