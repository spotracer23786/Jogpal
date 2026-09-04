package com.jogpal.app.data.passport

import com.jogpal.app.domain.passport.*
import com.jogpal.app.domain.run.RunRepository
import com.jogpal.app.data.run.RunRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PassportRepository(
    private val runRepository: RunRepository = RunRepositoryImpl()
) {

    fun getPassportData(userName: String = "Runner"): Flow<PassportData> {
        return runRepository.getRunHistory().map { completedRuns ->
            val totalDistance = completedRuns.sumOf { it.actualDistanceKm ?: it.distanceKm }.let {
                if (it == 0.0) 384.6 else it
            }
            val jogCount = if (completedRuns.isEmpty()) 127 else completedRuns.size

            PassportData(
                username = userName.ifEmpty { "Jogpal Runner" },
                level = 18,
                totalJogs = jogCount,
                totalDistanceKm = totalDistance,
                streakDays = 23,
                personalRecordCount = 14,
                unlockedMilestones = 23,
                totalMilestones = 30,
                milestones = listOf(
                    JourneyMilestone("m1", "First Jog", true, "Jan 12, 2026"),
                    JourneyMilestone("m2", "10 KM Completed", true, "Feb 01, 2026"),
                    JourneyMilestone("m3", "First Partner Jog", true, "Feb 14, 2026"),
                    JourneyMilestone("m4", "50 KM Completed", true, "Mar 20, 2026"),
                    JourneyMilestone("m5", "First Personal Record", true, "Apr 05, 2026"),
                    JourneyMilestone("m6", "100 KM Club", true, "Jun 18, 2026"),
                    JourneyMilestone("m7", "500 KM Club", false, null)
                ),
                locationStamps = listOf(
                    LocationStamp("s1", "TRICHY", 12, true, "📍"),
                    LocationStamp("s2", "CHENNAI", 4, true, "📍"),
                    LocationStamp("s3", "MADURAI", 2, true, "📍"),
                    LocationStamp("s4", "BANGALORE", 0, false, "🔒"),
                    LocationStamp("s5", "HYDERABAD", 0, false, "🔒"),
                    LocationStamp("s6", "MUMBAI", 0, false, "🔒")
                ),
                achievements = listOf(
                    AchievementBadge("a1", "First Step", "Complete your very first jog", "🏃", RarityLevel.COMMON, true, "Jan 12"),
                    AchievementBadge("a2", "Night Runner", "Complete a run after 9 PM", "🌙", RarityLevel.RARE, true, "Feb 18"),
                    AchievementBadge("a3", "30 Day Streak", "Maintain a 30-day streak", "🔥", RarityLevel.RARE, true, "Aug 20"),
                    AchievementBadge("a4", "100 KM Club", "Cross 100 total kilometers", "💯", RarityLevel.EPIC, true, "Jun 18"),
                    AchievementBadge("a5", "1,000 KM Club", "Cross 1,000 total kilometers", "👑", RarityLevel.LEGENDARY, false, null),
                    AchievementBadge("a6", "Ghost Defeater", "Defeat your shadow in Ghost Mode", "👻", RarityLevel.EPIC, true, "Aug 30")
                ),
                personalRecords = listOf(
                    PersonalRecordItem("Fastest 1 KM", "4:12/km", "Aug 15, 2026", true),
                    PersonalRecordItem("Fastest 3 KM", "14:20", "Aug 22, 2026", false),
                    PersonalRecordItem("Fastest 5 KM", "24:45", "Aug 28, 2026", true),
                    PersonalRecordItem("Longest Jog", "12.4 km", "Jul 30, 2026", false),
                    PersonalRecordItem("Highest Weekly Distance", "42.8 km", "Aug 24, 2026", false)
                ),
                socialMilestones = listOf(
                    SocialMilestone("Partner Jogs", "28 Jogs", "🤝"),
                    SocialMilestone("Challenges Won", "19 Battles", "⚔️"),
                    SocialMilestone("Jog Rooms Joined", "8 Squads", "👥"),
                    SocialMilestone("Community Events", "12 Badges", "🏆")
                )
            )
        }
    }
}
