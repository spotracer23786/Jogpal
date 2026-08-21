package com.jogpal.app.domain.run

import kotlinx.coroutines.flow.Flow

interface LiveRunRepository {
    suspend fun shareLocation(runId: String, location: LiveLocation): Result<Unit>
    fun getPartnerLocation(runId: String, partnerUid: String): Flow<LiveLocation?>
}
