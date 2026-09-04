package com.jogpal.app.domain.passport

enum class RarityLevel {
    COMMON, RARE, EPIC, LEGENDARY
}

data class JourneyMilestone(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val completedDate: String? = null
)

data class LocationStamp(
    val id: String,
    val cityName: String,
    val jogCount: Int,
    val isUnlocked: Boolean,
    val iconEmoji: String = "📍"
)

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val rarity: RarityLevel,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null
)

data class PersonalRecordItem(
    val label: String,
    val value: String,
    val dateAchieved: String,
    val isRecent: Boolean = false
)

data class SocialMilestone(
    val label: String,
    val value: String,
    val iconEmoji: String
)

data class PassportData(
    val username: String,
    val avatarUrl: String? = null,
    val level: Int = 18,
    val totalJogs: Int = 127,
    val totalDistanceKm: Double = 384.6,
    val streakDays: Int = 23,
    val personalRecordCount: Int = 14,
    val unlockedMilestones: Int = 23,
    val totalMilestones: Int = 30,
    val milestones: List<JourneyMilestone> = emptyList(),
    val locationStamps: List<LocationStamp> = emptyList(),
    val achievements: List<AchievementBadge> = emptyList(),
    val personalRecords: List<PersonalRecordItem> = emptyList(),
    val socialMilestones: List<SocialMilestone> = emptyList()
)
