package com.jogpal.app.core.common

import com.jogpal.app.domain.user.UserProfile

object CompatibilityEngine {
    
    fun calculateScore(user: UserProfile, target: UserProfile): Int {
        var score = 0
        
        // 1. Goal (30 pts) - Exact match, case-insensitive
        if (user.runningGoal?.trim()?.equals(target.runningGoal?.trim(), ignoreCase = true) == true) {
            score += 30
        }
        
        // 2. Experience (20 pts) - Exact match, case-insensitive
        if (user.experienceLevel?.trim()?.equals(target.experienceLevel?.trim(), ignoreCase = true) == true) {
            score += 20
        }
        
        // 3. Distance (20 pts) - Exact match, case-insensitive
        if (user.preferredDistance?.trim()?.equals(target.preferredDistance?.trim(), ignoreCase = true) == true) {
            score += 20
        }
        
        // 4. Pace (15 pts) - Exact match, case-insensitive
        if (user.preferredPace?.trim()?.equals(target.preferredPace?.trim(), ignoreCase = true) == true) {
            score += 15
        }
        
        // 5. Running Days (15 pts) - Percentage match
        val userDays = user.runningDays?.map { it.trim().lowercase() }?.toSet() ?: emptySet()
        val targetDays = target.runningDays?.map { it.trim().lowercase() }?.toSet() ?: emptySet()
        
        if (userDays.isNotEmpty() && targetDays.isNotEmpty()) {
            val commonDays = userDays.intersect(targetDays)
            if (commonDays.isNotEmpty()) {
                val maxPossible = maxOf(userDays.size, targetDays.size)
                val ratio = commonDays.size.toDouble() / maxPossible
                score += (ratio * 15).toInt()
            }
        }
        
        return score.coerceIn(0, 100)
    }
}
