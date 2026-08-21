package com.jogpal.app.domain.user

interface UserRepository {
    suspend fun createUserProfile(profile: UserProfile): Result<Unit>
    suspend fun getUserProfile(uid: String): Result<UserProfile?>
}
