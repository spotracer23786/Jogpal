package com.jogpal.app.domain.user

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileCompleted: Boolean = false,
    val runningGoal: String? = null,
    val experienceLevel: String? = null,
    val preferredDistance: String? = null,
    val preferredPace: String? = null,
    val runningDays: List<String>? = null,
    val location: UserLocation? = null,
    val locationSharingEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
