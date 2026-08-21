package com.jogpal.app.domain.location

import com.jogpal.app.domain.user.UserLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun hasLocationPermission(): Boolean
    fun isLocationEnabled(): Boolean
    suspend fun getCurrentLocation(): Result<UserLocation>
    fun getLocationUpdates(): Flow<UserLocation>
}
