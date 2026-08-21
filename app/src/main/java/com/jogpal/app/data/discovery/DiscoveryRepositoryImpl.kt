package com.jogpal.app.data.discovery

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jogpal.app.core.common.LocationUtils
import com.jogpal.app.data.mapper.toUserProfile
import com.jogpal.app.domain.discovery.DiscoveryProfile
import com.jogpal.app.domain.discovery.DiscoveryRepository
import com.jogpal.app.domain.user.UserProfile
import kotlinx.coroutines.tasks.await

class DiscoveryRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : DiscoveryRepository {

    override suspend fun findNearbyRunners(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<UserProfile>> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
        
        return try {
            // Simplified geohash prefix query (first 4 chars covers ~40km)
            val userGeohash = LocationUtils.encodeGeohash(latitude, longitude)
            val prefix = userGeohash.substring(0, 4)
            
            val snapshot = firestore.collection("runnerDiscovery")
                .whereEqualTo("locationSharingEnabled", true)
                .whereGreaterThanOrEqualTo("geohash", prefix)
                .whereLessThanOrEqualTo("geohash", prefix + "\uf8ff")
                .orderBy("geohash")
                .get()
                .await()
                
            val runners = snapshot.toObjects(DiscoveryProfile::class.java)
                .filter { it.uid != currentUid && it.geohash.isNotEmpty() }
                .map { discoveryProfile ->
                    // Calculate distance using center of discovery geohash vs current user's real location
                    // (User's real location is private to them, discovery geohash center is the fuzzed public point)
                    val runnerCenter = LocationUtils.decodeGeohash(discoveryProfile.geohash)
                    val distance = LocationUtils.calculateDistanceKm(
                        latitude, longitude,
                        runnerCenter.first, runnerCenter.second
                    )
                    discoveryProfile to distance
                }
                .filter { it.second <= radiusKm }
                .sortedBy { it.second }
                .map { it.first.toUserProfile() }
                
            Result.success(runners)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
