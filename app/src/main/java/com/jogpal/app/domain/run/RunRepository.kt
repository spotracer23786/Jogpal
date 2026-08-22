package com.jogpal.app.domain.run

import kotlinx.coroutines.flow.Flow

interface RunRepository {
    suspend fun createRunPlan(plan: RunPlan): Result<Unit>
    suspend fun updateRunStatus(runId: String, newStatus: RunStatus): Result<Unit>
    suspend fun completeRun(
        runId: String,
        actualDistanceKm: Double,
        actualDurationSeconds: Long,
        calories: Int,
        actualPolyline: String?
    ): Result<Unit>
    
    fun getUpcomingRuns(): Flow<List<RunPlan>>
    fun getIncomingInvitations(): Flow<List<RunPlan>>
    fun getRunHistory(): Flow<List<RunPlan>>
    fun getRunPlan(runId: String): Flow<RunPlan?>
}
