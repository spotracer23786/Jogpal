package com.jogpal.app.domain.chat

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(partnerUid: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(partnerUid: String, content: String): Result<Unit>
}
