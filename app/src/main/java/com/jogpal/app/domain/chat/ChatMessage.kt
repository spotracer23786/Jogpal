package com.jogpal.app.domain.chat

data class ChatMessage(
    val id: String = "",
    val senderUid: String = "",
    val receiverUid: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)
