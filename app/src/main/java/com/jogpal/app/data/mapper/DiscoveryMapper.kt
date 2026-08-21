package com.jogpal.app.data.mapper

import com.jogpal.app.domain.discovery.DiscoveryProfile
import com.jogpal.app.domain.user.UserProfile

fun UserProfile.toDiscoveryProfile(): DiscoveryProfile {
    return DiscoveryProfile(
        uid = uid,
        displayName = name,
        runningGoal = runningGoal,
        experienceLevel = experienceLevel,
        preferredDistance = preferredDistance,
        preferredPace = preferredPace,
        runningDays = runningDays,
        locationSharingEnabled = locationSharingEnabled,
        geohash = location?.geohash ?: "",
        updatedAt = updatedAt
    )
}

fun DiscoveryProfile.toUserProfile(): UserProfile {
    return UserProfile(
        uid = uid,
        name = displayName,
        runningGoal = runningGoal,
        experienceLevel = experienceLevel,
        preferredDistance = preferredDistance,
        preferredPace = preferredPace,
        runningDays = runningDays,
        locationSharingEnabled = locationSharingEnabled,
        location = if (geohash.isNotEmpty()) {
            // Reconstruct approximate location from geohash center
            val center = com.jogpal.app.core.common.LocationUtils.decodeGeohash(geohash)
            com.jogpal.app.domain.user.UserLocation(
                latitude = center.first,
                longitude = center.second,
                geohash = geohash,
                updatedAt = updatedAt
            )
        } else null,
        updatedAt = updatedAt
    )
}
