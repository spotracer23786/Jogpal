package com.jogpal.app.data.run

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.jogpal.app.domain.run.LiveLocation
import com.jogpal.app.domain.run.LiveRunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class LiveRunRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LiveRunRepository {

    override suspend fun shareLocation(runId: String, location: LiveLocation): Result<Unit> {
        return try {
            firestore.collection("activeRuns")
                .document(runId)
                .collection("locations")
                .document(location.uid)
                .set(location)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPartnerLocation(runId: String, partnerUid: String): Flow<LiveLocation?> {
        return firestore.collection("activeRuns")
            .document(runId)
            .collection("locations")
            .document(partnerUid)
            .snapshots()
            .map { it.toObject(LiveLocation::class.java) }
    }
}
