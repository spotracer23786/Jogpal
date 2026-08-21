package com.jogpal.app.data.matching

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.jogpal.app.domain.matching.Match
import com.jogpal.app.domain.matching.MatchingRepository
import com.jogpal.app.domain.matching.RequestStatus
import com.jogpal.app.domain.matching.RunRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MatchingRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : MatchingRepository {

    private val TAG = "MatchingRepo"

    override val currentUserUid: String?
        get() = auth.currentUser?.uid

    override suspend fun sendRequest(receiverUid: String, compatibilityScore: Int): Result<Unit> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
        if (currentUid == receiverUid) return Result.failure(Exception("Cannot send request to yourself"))

        return try {
            Log.d(TAG, "Sending request from $currentUid to $receiverUid")
            
            // Check for existing pending request or active match
            val existingMatch = getMatchBetweenUsers(currentUid, receiverUid).getOrNull()
            if (existingMatch != null) return Result.failure(Exception("You are already matched with this runner"))

            val existingRequest = getRequestBetweenUsers(currentUid, receiverUid).getOrNull()
            if (existingRequest != null && existingRequest.status == RequestStatus.PENDING) {
                return Result.failure(Exception("A pending request already exists"))
            }

            val requestRef = firestore.collection("runRequests").document()
            val request = RunRequest(
                id = requestRef.id,
                senderUid = currentUid,
                receiverUid = receiverUid,
                status = RequestStatus.PENDING,
                compatibilityScore = compatibilityScore,
                timestamp = System.currentTimeMillis()
            )
            
            // Use toMap/Object mapping consistently
            requestRef.set(request).await()
            Log.d(TAG, "Request sent successfully: ${requestRef.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send request", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptRequest(requestId: String): Result<Unit> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))

        return try {
            Log.d(TAG, "Accepting request: $requestId as $currentUid")
            
            firestore.runTransaction { transaction ->
                val requestRef = firestore.collection("runRequests").document(requestId)
                val requestSnapshot = transaction.get(requestRef)
                
                // Read into object for consistency
                val request = requestSnapshot.toObject(RunRequest::class.java)
                    ?: throw Exception("Request document not found")

                if (request.receiverUid != currentUid) throw Exception("Unauthorized: You are not the receiver")
                if (request.status != RequestStatus.PENDING) throw Exception("Invalid request status: ${request.status}")

                // 1. Update request status
                transaction.update(requestRef, "status", RequestStatus.ACCEPTED.name)

                // 2. Create Match
                val uids = listOf(request.senderUid, request.receiverUid).sorted()
                val matchId = "${uids[0]}_${uids[1]}"
                val matchRef = firestore.collection("matches").document(matchId)
                
                val match = Match(
                    id = matchId,
                    participantUids = uids,
                    createdAt = System.currentTimeMillis()
                )
                transaction.set(matchRef, match)
            }.await()
            
            Log.d(TAG, "Request accepted and match created: $requestId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Transaction failed for acceptRequest", e)
            Result.failure(e)
        }
    }

    override suspend fun declineRequest(requestId: String): Result<Unit> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))

        return try {
            val requestRef = firestore.collection("runRequests").document(requestId)
            val snapshot = requestRef.get().await()
            val request = snapshot.toObject(RunRequest::class.java) ?: throw Exception("Request not found")
            
            if (request.receiverUid != currentUid) throw Exception("Unauthorized")

            requestRef.update("status", RequestStatus.DECLINED.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSentRequests(): Flow<List<RunRequest>> {
        val currentUid = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return firestore.collection("runRequests")
            .whereEqualTo("senderUid", currentUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(RunRequest::class.java) }
    }

    override fun getReceivedRequests(): Flow<List<RunRequest>> {
        val currentUid = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return firestore.collection("runRequests")
            .whereEqualTo("receiverUid", currentUid)
            .whereEqualTo("status", RequestStatus.PENDING.name)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(RunRequest::class.java) }
    }

    override fun getMatches(): Flow<List<Match>> {
        val currentUid = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return firestore.collection("matches")
            .whereArrayContains("participantUids", currentUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(Match::class.java) }
    }

    override suspend fun getRequestBetweenUsers(uid1: String, uid2: String): Result<RunRequest?> {
        return try {
            val q1 = firestore.collection("runRequests")
                .whereEqualTo("senderUid", uid1)
                .whereEqualTo("receiverUid", uid2)
                .limit(1)
                .get().await()
            
            if (!q1.isEmpty) return Result.success(q1.toObjects(RunRequest::class.java).first())

            val q2 = firestore.collection("runRequests")
                .whereEqualTo("senderUid", uid2)
                .whereEqualTo("receiverUid", uid1)
                .limit(1)
                .get().await()
            
            Result.success(if (q2.isEmpty) null else q2.toObjects(RunRequest::class.java).first())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getMatchBetweenUsers(uid1: String, uid2: String): Result<Match?> {
        return try {
            val uids = listOf(uid1, uid2).sorted()
            val matchId = "${uids[0]}_${uids[1]}"
            val doc = firestore.collection("matches").document(matchId).get().await()
            Result.success(if (doc.exists()) doc.toObject(Match::class.java) else null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
