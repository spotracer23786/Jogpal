package com.jogpal.app.data.profile

import com.google.firebase.firestore.FirebaseFirestore
import com.jogpal.app.data.mapper.toDiscoveryProfile
import com.jogpal.app.data.mapper.toUserProfile
import com.jogpal.app.domain.discovery.DiscoveryProfile
import com.jogpal.app.domain.profile.ProfileRepository
import com.jogpal.app.domain.user.UserProfile
import kotlinx.coroutines.tasks.await

class ProfileRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProfileRepository {

    override suspend fun getProfile(uid: String): Result<UserProfile?> {
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

    override suspend fun getDiscoveryProfile(uid: String): Result<UserProfile?> {
        return try {
            val snapshot = firestore.collection("runnerDiscovery")
                .document(uid)
                .get()
                .await()
            val discoveryProfile = snapshot.toObject(DiscoveryProfile::class.java)
            Result.success(discoveryProfile?.toUserProfile())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(profile: UserProfile): Result<Unit> {
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
}
