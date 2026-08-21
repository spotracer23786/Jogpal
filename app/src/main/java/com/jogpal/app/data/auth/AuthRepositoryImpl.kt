package com.jogpal.app.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.jogpal.app.domain.auth.AuthRepository
import com.jogpal.app.domain.user.UserProfile
import com.jogpal.app.domain.user.UserRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository
) : AuthRepository {

    override val currentUserUid: String?
        get() = auth.currentUser?.uid

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("User creation failed"))
            
            val profile = UserProfile(
                uid = uid,
                name = name,
                email = email
            )
            
            userRepository.createUserProfile(profile).fold(
                onSuccess = { Result.success(uid) },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Login failed"))
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        auth.signOut()
    }
}
