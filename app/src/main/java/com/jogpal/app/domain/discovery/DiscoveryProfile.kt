package com.jogpal.app.domain.discovery

data class DiscoveryProfile(
    val uid: String = "",
    val displayName: String = "",
    val runningGoal: String? = null,
    val experienceLevel: String? = null,
    val preferredDistance: String? = null,
    val preferredPace: String? = null,
    val runningDays: List<String>? = null,
    val locationSharingEnabled: Boolean = false,
    val geohash: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
