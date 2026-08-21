package com.jogpal.app.domain.auth

import com.jogpal.app.domain.user.UserProfile

interface AuthRepository {
    val currentUserUid: String?
    fun isUserLoggedIn(): Boolean
    suspend fun signUp(name: String, email: String, password: String): Result<String>
    suspend fun login(email: String, password: String): Result<String>
    fun logout()
}
