package com.jogpal.app.domain.user

data class UserLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geohash: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
