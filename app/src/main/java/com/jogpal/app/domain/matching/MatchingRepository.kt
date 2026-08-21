package com.jogpal.app.domain.matching

import kotlinx.coroutines.flow.Flow

interface MatchingRepository {
    val currentUserUid: String?
    suspend fun sendRequest(receiverUid: String, compatibilityScore: Int): Result<Unit>
    suspend fun acceptRequest(requestId: String): Result<Unit>
    suspend fun declineRequest(requestId: String): Result<Unit>
    
    fun getSentRequests(): Flow<List<RunRequest>>
    fun getReceivedRequests(): Flow<List<RunRequest>>
    fun getMatches(): Flow<List<Match>>
    
    suspend fun getRequestBetweenUsers(uid1: String, uid2: String): Result<RunRequest?>
}
