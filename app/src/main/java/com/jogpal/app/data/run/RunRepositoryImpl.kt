package com.jogpal.app.data.run

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.jogpal.app.domain.run.RunPlan
import com.jogpal.app.domain.run.RunRepository
import com.jogpal.app.domain.run.RunStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class RunRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : RunRepository {

    private val TAG = "RunRepo"

    override suspend fun createRunPlan(plan: RunPlan): Result<Unit> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
        if (plan.creatorUid != currentUid) return Result.failure(Exception("Unauthorized creator"))

        return try {
            val docRef = firestore.collection("runPlans").document()
            val finalPlan = plan.copy(
                id = docRef.id, 
                status = RunStatus.PENDING, 
                participantUids = listOf(plan.creatorUid, plan.partnerUid),
                createdAt = System.currentTimeMillis(), 
                updatedAt = System.currentTimeMillis()
            )
            docRef.set(finalPlan).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create run plan", e)
            Result.failure(e)
        }
    }

    override suspend fun updateRunStatus(runId: String, newStatus: RunStatus): Result<Unit> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))

        return try {
            Log.d(TAG, "Starting updateRunStatus: $runId to $newStatus by $currentUid")
            
            firestore.runTransaction { transaction ->
                val docRef = firestore.collection("runPlans").document(runId)
                val snapshot = transaction.get(docRef)
                
                if (!snapshot.exists()) throw Exception("Run plan not found")
                
                val plan = snapshot.toObject(RunPlan::class.java) ?: throw Exception("Failed to parse run plan")

                val isCreator = plan.creatorUid == currentUid
                val isPartner = plan.partnerUid == currentUid
                
                Log.d(TAG, "Plan context: creator=${plan.creatorUid}, partner=${plan.partnerUid}, currentStatus=${plan.status}")

                if (!isCreator && !isPartner) throw Exception("Unauthorized: User not in participant list")

                // State machine validation
                val currentStatus = plan.status
                val isValid = when (newStatus) {
                    RunStatus.ACCEPTED -> isPartner && currentStatus == RunStatus.PENDING
                    RunStatus.DECLINED -> isPartner && currentStatus == RunStatus.PENDING
                    RunStatus.ACTIVE -> (isCreator || isPartner) && currentStatus == RunStatus.ACCEPTED
                    RunStatus.CANCELLED -> (isCreator && (currentStatus == RunStatus.PENDING || currentStatus == RunStatus.ACCEPTED)) || (isPartner && currentStatus == RunStatus.ACCEPTED) || currentStatus == RunStatus.ACTIVE
                    RunStatus.COMPLETED -> (isCreator || isPartner) && (currentStatus == RunStatus.ACCEPTED || currentStatus == RunStatus.ACTIVE)
                    else -> false
                }

                if (!isValid) throw Exception("Invalid state transition from $currentStatus to $newStatus for role ${if(isCreator) "CREATOR" else "PARTNER"}")

                // Perform update
                transaction.update(docRef, "status", newStatus.name)
                transaction.update(docRef, "updatedAt", System.currentTimeMillis())
                null
            }.await()
            
            Log.d(TAG, "Successfully updated run status to $newStatus")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Transaction failed for updateRunStatus", e)
            Result.failure(e)
        }
    }

    override fun getUpcomingRuns(): Flow<List<RunPlan>> {
        val currentUid = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return firestore.collection("runPlans")
            .whereArrayContains("participantUids", currentUid)
            .whereIn("status", listOf(RunStatus.ACCEPTED.name, RunStatus.ACTIVE.name))
            .orderBy("date", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(RunPlan::class.java) }
    }

    override fun getIncomingInvitations(): Flow<List<RunPlan>> {
        val currentUid = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return firestore.collection("runPlans")
            .whereEqualTo("partnerUid", currentUid)
            .whereEqualTo("status", RunStatus.PENDING.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(RunPlan::class.java) }
    }

    override fun getRunPlan(runId: String): Flow<RunPlan?> {
        return firestore.collection("runPlans")
            .document(runId)
            .snapshots()
            .map { it.toObject(RunPlan::class.java) }
    }
}
