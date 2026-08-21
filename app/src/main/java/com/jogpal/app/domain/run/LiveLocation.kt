package com.jogpal.app.domain.run

data class LiveLocation(
    val uid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
