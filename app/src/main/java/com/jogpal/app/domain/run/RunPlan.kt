package com.jogpal.app.domain.run

data class RunPlan(
    val id: String = "",
    val creatorUid: String = "",
    val partnerUid: String = "",
    val participantUids: List<String> = emptyList(),
    val date: String = "", // YYYY-MM-DD
    val startTime: String = "", // HH:mm
    val distanceKm: Double = 0.0,
    val pace: String = "",
    val title: String = "",
    val notes: String = "",
    val status: RunStatus = RunStatus.PENDING,
    
    // Route Details
    val startLat: Double? = null,
    val startLng: Double? = null,
    val endLat: Double? = null,
    val endLng: Double? = null,
    val destinationName: String? = null,
    val encodedPolyline: String? = null,
    val estimatedDurationMinutes: Int? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RunStatus {
    PENDING, ACCEPTED, ACTIVE, COMPLETED, CANCELLED, DECLINED
}
