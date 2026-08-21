package com.jogpal.app.data.user

import com.google.firebase.firestore.FirebaseFirestore
import com.jogpal.app.data.mapper.toDiscoveryProfile
import com.jogpal.app.domain.user.UserProfile
import com.jogpal.app.domain.user.UserRepository
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

    override suspend fun createUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            val userRef = firestore.collection("users").document(profile.uid)
            batch.set(userRef, profile)
            
            val discoveryRef = firestore.collection("runnerDiscovery").document(profile.uid)
            batch.set(discoveryRef, profile.toDiscoveryProfile())
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            val profile = snapshot.toObject(UserProfile::class.java)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
