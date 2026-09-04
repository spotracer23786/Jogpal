package com.jogpal.app.features.sos

import java.util.UUID

enum class SOSStatus {
    IDLE,
    CONFIRMATION,
    COUNTDOWN,
    ACTIVE,
    CANCELLED
}

data class TrustedContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isEnabled: Boolean = true
)

data class EmergencyProfile(
    val primaryContactName: String = "",
    val primaryContactPhone: String = "",
    val preferredLanguage: String = "English",
    val importantNotes: String = "",
    val medicalInfo: String = "" // Optional, clearly tagged as user-provided
)

enum class SOSEventType {
    ACTIVE_SOS,
    TEST_MODE,
    CANCELLED_SOS
}

data class SOSEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val dateString: String,
    val type: SOSEventType,
    val location: String = "37.7749° N, 122.4194° W",
    val contactsNotifiedCount: Int = 0
)

data class LiveLocationData(
    val latitude: Double = 37.7749,
    val longitude: Double = 122.4194,
    val accuracyMeters: Float = 4.5f,
    val isGpsAvailable: Boolean = true,
    val lastUpdatedSecondsAgo: Int = 3,
    val movementStatus: String = "Jogging (Slow Pace)",
    val batteryPercentage: Int = 84,
    val distanceKm: Double = 3.42,
    val durationSeconds: Long = 1245
)
