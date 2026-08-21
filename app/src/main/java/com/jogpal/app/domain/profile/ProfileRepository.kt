package com.jogpal.app.domain.profile

import com.jogpal.app.domain.user.UserProfile

interface ProfileRepository {
    suspend fun getProfile(uid: String): Result<UserProfile?>
    suspend fun getDiscoveryProfile(uid: String): Result<UserProfile?>
    suspend fun updateProfile(profile: UserProfile): Result<Unit>
}
